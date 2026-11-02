import SwiftUI

struct CounterView: View {
    @EnvironmentObject private var store: PrayerStore
    @State private var showTopicEditor = false
    @State private var showTargetEditor = false
    @State private var showResetConfirmation = false

    private let tapFeedback = UIImpactFeedbackGenerator(style: .light)

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    topicCard
                    countCard
                    countButton
                    controls
                    verse
                }
                .padding()
                .frame(maxWidth: 560)
                .frame(maxWidth: .infinity)
            }
            .background(Color.offWhite.ignoresSafeArea())
            .navigationTitle("Prayer Proclamation")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Picker(store.text("Language", "Langue"), selection: $store.language) {
                            ForEach(AppLanguage.allCases) { Text($0.displayName).tag($0) }
                        }
                    } label: {
                        Image(systemName: "globe")
                    }
                }
            }
            #if DEBUG
            .onAppear {
                switch UserDefaults.standard.string(forKey: "demoScreen") {
                case "topic": showTopicEditor = true
                case "target": showTargetEditor = true
                default: break
                }
            }
            #endif
            .sheet(isPresented: $showTopicEditor) { TopicEditor() }
            .sheet(isPresented: $showTargetEditor) { TargetEditor() }
            .confirmationDialog(
                store.text("Reset Current Session?", "Réinitialiser la session en cours ?"),
                isPresented: $showResetConfirmation,
                titleVisibility: .visible
            ) {
                Button(store.text("Reset Session", "Réinitialiser la session"), role: .destructive) {
                    store.resetSession()
                }
            } message: {
                Text(store.text(
                    "Your saved cumulative count (\(store.savedCumulativeCount)) will NOT be deleted.",
                    "Votre total cumulé sauvegardé (\(store.savedCumulativeCount)) NE SERA PAS supprimé."
                ))
            }
            .alert(
                store.text("Saved", "Enregistré"),
                isPresented: Binding(get: { store.saveMessage != nil }, set: { if !$0 { store.saveMessage = nil } })
            ) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(store.saveMessage ?? "")
            }
        }
    }

    private var topicCard: some View {
        Button { showTopicEditor = true } label: {
            VStack(alignment: .leading, spacing: 6) {
                Text(store.text("PRAYER TOPIC / PROCLAMATION", "SUJET DE PRIÈRE / PROCLAMATION"))
                    .font(.caption.weight(.semibold))
                    .foregroundColor(.softGold)
                HStack {
                    Text(store.prayerTopic)
                        .font(.headline)
                        .foregroundColor(.white)
                        .multilineTextAlignment(.leading)
                    Spacer()
                    Image(systemName: "pencil").foregroundColor(.softGold)
                }
            }
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.deepNavy, in: RoundedRectangle(cornerRadius: 16))
        }
    }

    private var statusText: String {
        if store.isTargetReached { return store.text("TARGET ACHIEVED", "OBJECTIF ATTEINT") }
        return store.isTimerRunning
            ? store.text("PRAYING NOW", "EN PRIÈRE")
            : store.text("SESSION PAUSED", "SESSION EN PAUSE")
    }

    private var countCard: some View {
        VStack(spacing: 12) {
            Text(statusText)
                .font(.caption.weight(.bold))
                .foregroundColor(store.isTargetReached ? .accentGreen : .textMuted)
            Text("\(store.totalCount)")
                .font(.system(size: 64, weight: .bold, design: .rounded))
                .foregroundColor(.deepNavy)
                .monospacedDigit()
                .minimumScaleFactor(0.5)
                .lineLimit(1)
            Button { showTargetEditor = true } label: {
                Text(store.text("Target: \(store.targetCount)", "Objectif : \(store.targetCount)"))
                    .font(.subheadline.weight(.semibold))
            }
            ProgressView(value: store.progress)
            HStack {
                stat(store.text("Session Timer", "Chronomètre"), formatDuration(store.sessionDurationSeconds))
                Spacer()
                stat(store.text("This Session", "Cette session"), "+\(store.currentSessionCount)")
                Spacer()
                stat(store.text("Saved Base", "Base enregistrée"), "\(store.savedCumulativeCount)")
            }
        }
        .padding()
        .background(Color.white, in: RoundedRectangle(cornerRadius: 16))
    }

    private func stat(_ label: String, _ value: String) -> some View {
        VStack(spacing: 2) {
            Text(value).font(.headline).foregroundColor(.deepNavy).monospacedDigit()
            Text(label).font(.caption2).foregroundColor(.textMuted)
        }
    }

    private var countButton: some View {
        Button {
            tapFeedback.impactOccurred()
            store.increment()
        } label: {
            Text(store.text("COUNT +1", "COMPTER +1"))
                .font(.title2.weight(.bold))
                .foregroundColor(.deepNavy)
                .frame(maxWidth: .infinity, minHeight: 96)
                .background(Color.softGold, in: RoundedRectangle(cornerRadius: 20))
        }
    }

    private var controls: some View {
        VStack(spacing: 10) {
            HStack(spacing: 10) {
                Button {
                    store.isTimerRunning ? store.pauseTimer() : store.startTimer()
                } label: {
                    Label(
                        store.isTimerRunning ? store.text("PAUSE", "PAUSE") : store.text("START", "DÉMARRER"),
                        systemImage: store.isTimerRunning ? "pause.fill" : "play.fill"
                    )
                    .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)

                Button { store.saveSession() } label: {
                    Label(store.text("SAVE", "ENREGISTRER"), systemImage: "square.and.arrow.down.fill")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
            }
            Button(role: .destructive) { showResetConfirmation = true } label: {
                Text(store.text("RESET SESSION", "RÉINITIALISER LA SESSION")).font(.footnote.weight(.semibold))
            }
        }
    }

    private var verse: some View {
        Text(store.text(
            "“Death and life are in the power of the tongue”\n— Proverbs 18:21",
            "« La mort et la vie sont au pouvoir de la langue »\n— Proverbes 18:21"
        ))
        .font(.footnote.italic())
        .foregroundColor(.textMuted)
        .multilineTextAlignment(.center)
    }
}

private struct TopicEditor: View {
    @EnvironmentObject private var store: PrayerStore
    @Environment(\.dismiss) private var dismiss
    @State private var draft = ""

    var body: some View {
        NavigationStack {
            Form {
                Section(store.text("Prayer topic", "Sujet de prière")) {
                    TextField(store.text("e.g. Jesus Christ is the Lord", "ex. Jésus-Christ est Seigneur"), text: $draft, axis: .vertical)
                }
                Section(store.text("Suggestions", "Suggestions")) {
                    ForEach(store.language.presetTopics, id: \.self) { topic in
                        Button(topic) { draft = topic }
                    }
                }
            }
            .navigationTitle(store.text("Topic", "Sujet"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(store.text("Cancel", "Annuler")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("OK") {
                        let trimmed = draft.trimmingCharacters(in: .whitespacesAndNewlines)
                        if !trimmed.isEmpty { store.prayerTopic = trimmed }
                        dismiss()
                    }
                }
            }
            .onAppear { draft = store.prayerTopic }
        }
    }
}

private struct TargetEditor: View {
    @EnvironmentObject private var store: PrayerStore
    @Environment(\.dismiss) private var dismiss
    @State private var draft = ""

    var body: some View {
        NavigationStack {
            Form {
                Section(store.text("Target proclamations", "Objectif de proclamations")) {
                    TextField(store.text("e.g. 1000", "ex. 1000"), text: $draft)
                        .keyboardType(.numberPad)
                }
                Section(store.text("Quick Targets", "Objectifs rapides")) {
                    ForEach(presetTargets, id: \.self) { target in
                        Button("\(target)") { draft = "\(target)" }
                    }
                }
            }
            .navigationTitle(store.text("Target", "Objectif"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(store.text("Cancel", "Annuler")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("OK") {
                        if let value = Int(draft) { store.setTarget(value) }
                        dismiss()
                    }
                }
            }
            .onAppear { draft = "\(store.targetCount)" }
        }
    }
}
