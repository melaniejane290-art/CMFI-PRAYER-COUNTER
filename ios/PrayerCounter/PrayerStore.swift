import Foundation
import SwiftUI

/// Counter state and records. Everything stays on the device, in one JSON file.
@MainActor
final class PrayerStore: ObservableObject {
    private struct Snapshot: Codable {
        var language: AppLanguage
        var prayerTopic: String
        var targetCount: Int
        var savedCumulativeCount: Int
        var proclamations: [SavedProclamation]
        var sessions: [PrayerSessionRecord]
    }

    @Published var language: AppLanguage { didSet { persist() } }
    @Published var prayerTopic: String { didSet { persist() } }
    @Published private(set) var targetCount: Int
    @Published private(set) var savedCumulativeCount: Int
    @Published private(set) var proclamations: [SavedProclamation]
    @Published private(set) var sessions: [PrayerSessionRecord]

    @Published private(set) var currentSessionCount = 0
    @Published private(set) var sessionDurationSeconds = 0
    @Published private(set) var isTimerRunning = false
    @Published var saveMessage: String?

    private var timer: Timer?
    private var timerStart: Date?
    private var secondsBeforeStart = 0
    private let fileURL: URL

    init(fileURL: URL? = nil) {
        let url = fileURL ?? FileManager.default
            .urls(for: .documentDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("prayer-data.json")
        self.fileURL = url

        if let data = try? Data(contentsOf: url),
           let snapshot = try? JSONDecoder().decode(Snapshot.self, from: data) {
            language = snapshot.language
            prayerTopic = snapshot.prayerTopic
            targetCount = snapshot.targetCount
            savedCumulativeCount = snapshot.savedCumulativeCount
            proclamations = snapshot.proclamations
            sessions = snapshot.sessions
        } else {
            let deviceLanguage: AppLanguage = Locale.preferredLanguages.first?.hasPrefix("fr") == true ? .french : .english
            language = deviceLanguage
            prayerTopic = deviceLanguage.presetTopics[0]
            targetCount = 1000
            savedCumulativeCount = 0
            proclamations = []
            sessions = []
        }
    }

    // MARK: Text

    func text(_ english: String, _ french: String) -> String {
        language.isFrench ? french : english
    }

    // MARK: Counter

    var totalCount: Int { savedCumulativeCount + currentSessionCount }

    var progress: Double {
        targetCount > 0 ? min(Double(totalCount) / Double(targetCount), 1) : 0
    }

    var isTargetReached: Bool { targetCount > 0 && totalCount >= targetCount }

    func increment() {
        if !isTimerRunning { startTimer() }
        currentSessionCount += 1
    }

    func setTarget(_ value: Int) {
        targetCount = max(1, value)
        persist()
    }

    func startTimer() {
        guard !isTimerRunning else { return }
        isTimerRunning = true
        timerStart = Date()
        secondsBeforeStart = sessionDurationSeconds
        let timer = Timer(timeInterval: 1, repeats: true) { [weak self] _ in
            Task { @MainActor in self?.refreshDuration() }
        }
        RunLoop.main.add(timer, forMode: .common)
        self.timer = timer
    }

    func pauseTimer() {
        refreshDuration()
        timer?.invalidate()
        timer = nil
        timerStart = nil
        isTimerRunning = false
    }

    /// Computed from dates so that time spent with the app in the background still counts.
    private func refreshDuration() {
        guard let timerStart else { return }
        sessionDurationSeconds = secondsBeforeStart + Int(Date().timeIntervalSince(timerStart))
    }

    /// Resets the current session only: the saved cumulative count is kept.
    func resetSession() {
        pauseTimer()
        currentSessionCount = 0
        sessionDurationSeconds = 0
        saveMessage = text(
            "Session timer and session count reset. Cumulative count (\(savedCumulativeCount)) preserved.",
            "Chronomètre et compte de session réinitialisés. Total cumulé (\(savedCumulativeCount)) préservé."
        )
    }

    func saveSession() {
        pauseTimer()
        let added = currentSessionCount
        let duration = sessionDurationSeconds
        let newTotal = savedCumulativeCount + added

        if added > 0 {
            let reached = targetCount > 0 && newTotal >= targetCount
            sessions.insert(
                PrayerSessionRecord(
                    topic: prayerTopic,
                    sessionCount: added,
                    durationSeconds: duration,
                    targetCount: targetCount,
                    languageCode: language.rawValue,
                    isTargetReached: reached
                ),
                at: 0
            )
            proclamations.insert(
                SavedProclamation(
                    topic: prayerTopic,
                    proclamationCount: newTotal,
                    durationSeconds: duration,
                    targetCount: targetCount,
                    languageCode: language.rawValue,
                    isTargetCompleted: reached
                ),
                at: 0
            )
        }

        savedCumulativeCount = newTotal
        currentSessionCount = 0
        sessionDurationSeconds = 0
        saveMessage = added > 0
            ? text(
                "Saved +\(added) proclamations (Session: \(formatDuration(duration))). Total: \(newTotal) / \(targetCount)",
                "+\(added) proclamations enregistrées (Session : \(formatDuration(duration))). Total : \(newTotal) / \(targetCount)"
            )
            : text(
                "Current progress saved. Total: \(newTotal) / \(targetCount)",
                "Progression enregistrée. Total : \(newTotal) / \(targetCount)"
            )
        persist()
    }

    // MARK: Saved proclamations

    func resume(_ saved: SavedProclamation) {
        pauseTimer()
        prayerTopic = saved.topic
        savedCumulativeCount = saved.proclamationCount
        if saved.targetCount > 0 { targetCount = saved.targetCount }
        currentSessionCount = 0
        sessionDurationSeconds = 0
        saveMessage = text(
            "“\(saved.topic)” loaded into counter with \(saved.proclamationCount) proclamations.",
            "« \(saved.topic) » chargé dans le compteur avec \(saved.proclamationCount) proclamations."
        )
        persist()
    }

    func updateNotes(of saved: SavedProclamation, to notes: String) {
        guard let index = proclamations.firstIndex(where: { $0.id == saved.id }) else { return }
        proclamations[index].notes = notes
        persist()
    }

    func deleteProclamation(_ saved: SavedProclamation) {
        proclamations.removeAll { $0.id == saved.id }
        persist()
    }

    // MARK: History

    func deleteSession(_ session: PrayerSessionRecord) {
        sessions.removeAll { $0.id == session.id }
        persist()
    }

    func clearHistory() {
        sessions.removeAll()
        persist()
    }

    /// Consecutive days with a session, ending today or yesterday.
    func currentStreak(calendar: Calendar = .current) -> Int {
        let days = Set(sessions.map { calendar.startOfDay(for: $0.timestamp) })
        var day = calendar.startOfDay(for: Date())
        if !days.contains(day) {
            guard let yesterday = calendar.date(byAdding: .day, value: -1, to: day), days.contains(yesterday) else { return 0 }
            day = yesterday
        }
        var streak = 0
        while days.contains(day) {
            streak += 1
            guard let previous = calendar.date(byAdding: .day, value: -1, to: day) else { break }
            day = previous
        }
        return streak
    }

    /// Proclamations per day over the last seven days, oldest first.
    func lastSevenDays(calendar: Calendar = .current) -> [(day: Date, count: Int)] {
        let today = calendar.startOfDay(for: Date())
        return (0..<7).reversed().compactMap { offset in
            guard let day = calendar.date(byAdding: .day, value: -offset, to: today) else { return nil }
            let count = sessions
                .filter { calendar.isDate($0.timestamp, inSameDayAs: day) }
                .reduce(0) { $0 + $1.sessionCount }
            return (day, count)
        }
    }

    func shareText(for session: PrayerSessionRecord) -> String {
        let date = session.timestamp.formatted(date: .abbreviated, time: .shortened)
        let pace = String(format: "%.1f / min", session.pacePerMinute)
        return text(
            "✝️ Prayer Proclamation Session\nTopic: \(session.topic)\nProclamations: +\(session.sessionCount)\nDuration: \(formatDuration(session.durationSeconds))\nPace: \(pace)\nDate: \(date)\n\n“Death and life are in the power of the tongue” (Proverbs 18:21)",
            "✝️ Temps de Prière & Proclamation\nSujet : \(session.topic)\nProclamations : +\(session.sessionCount)\nDurée : \(formatDuration(session.durationSeconds))\nRythme : \(pace)\nDate : \(date)\n\n« La mort et la vie sont au pouvoir de la langue » (Proverbes 18:21)"
        )
    }

    // MARK: Store screenshots

    #if DEBUG
    /// Sample content for the App Store screenshots; never compiled into a release build.
    static func demo(language: AppLanguage) -> PrayerStore {
        let file = FileManager.default.temporaryDirectory.appendingPathComponent("prayer-demo-\(UUID().uuidString).json")
        let store = PrayerStore(fileURL: file)
        store.language = language
        let topics = language.presetTopics
        store.prayerTopic = topics[0]
        store.targetCount = 1000
        store.savedCumulativeCount = 640
        store.currentSessionCount = 47
        store.sessionDurationSeconds = 754
        store.isTimerRunning = true

        let code = language.rawValue
        let plan = [(0, 0, 180, 1260), (1, 1, 240, 1500), (2, 2, 120, 840), (3, 3, 300, 1980), (4, 4, 150, 960), (6, 0, 90, 600)]
        store.sessions = plan.map { days, topic, count, seconds in
            PrayerSessionRecord(
                topic: topics[topic],
                sessionCount: count,
                durationSeconds: seconds,
                targetCount: 1000,
                timestamp: Date().addingTimeInterval(Double(-days) * 86_400 - 3_600),
                languageCode: code,
                isTargetReached: false
            )
        }
        store.proclamations = [
            SavedProclamation(
                topic: topics[0], proclamationCount: 1000, durationSeconds: 2_460, targetCount: 1000, languageCode: code,
                notes: store.text("Philippians 2:11. A time of great peace.", "Philippiens 2:11. Un temps de grande paix."),
                timestamp: Date().addingTimeInterval(-86_400), isTargetCompleted: true
            ),
            SavedProclamation(
                topic: topics[2], proclamationCount: 500, durationSeconds: 1_500, targetCount: 500, languageCode: code,
                notes: store.text("Isaiah 53:5.", "Ésaïe 53:5."),
                timestamp: Date().addingTimeInterval(-259_200), isTargetCompleted: true
            ),
            SavedProclamation(
                topic: topics[1], proclamationCount: 320, durationSeconds: 960, targetCount: 1000, languageCode: code,
                timestamp: Date().addingTimeInterval(-432_000), isTargetCompleted: false
            )
        ]
        return store
    }
    #endif

    // MARK: Persistence

    private func persist() {
        let snapshot = Snapshot(
            language: language,
            prayerTopic: prayerTopic,
            targetCount: targetCount,
            savedCumulativeCount: savedCumulativeCount,
            proclamations: proclamations,
            sessions: sessions
        )
        guard let data = try? JSONEncoder().encode(snapshot) else { return }
        try? data.write(to: fileURL, options: [.atomic, .completeFileProtection])
    }
}
