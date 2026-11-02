import Foundation
import SwiftUI

enum AppLanguage: String, Codable, CaseIterable, Identifiable {
    case english = "en"
    case french = "fr"

    var id: String { rawValue }
    var displayName: String { self == .english ? "English" : "Français" }
    var isFrench: Bool { self == .french }

    var presetTopics: [String] {
        switch self {
        case .english:
            return [
                "Jesus Christ is the Lord",
                "The Lord is my strength and shield",
                "By His stripes we are healed",
                "No weapon formed shall prosper",
                "The Lord will fight for you"
            ]
        case .french:
            return [
                "Jésus-Christ est Seigneur",
                "L'Éternel est ma force et mon bouclier",
                "Par ses meurtrissures nous sommes guéris",
                "Toute arme forgée contre toi sera sans effet",
                "L'Éternel combattra pour vous"
            ]
        }
    }
}

let presetTargets = [50, 100, 500, 1000, 2500, 5000, 10000]

/// A saved proclamation, with the same fields as the Android `SavedProclamation`.
struct SavedProclamation: Codable, Identifiable, Equatable {
    var id = UUID()
    var topic: String
    var proclamationCount: Int
    var durationSeconds: Int
    var targetCount: Int
    var languageCode: String
    var notes = ""
    var timestamp = Date()
    var isTargetCompleted: Bool
}

/// A prayer session log, with the same fields as the Android `PrayerSessionRecord`.
struct PrayerSessionRecord: Codable, Identifiable, Equatable {
    var id = UUID()
    var topic: String
    var sessionCount: Int
    var durationSeconds: Int
    var targetCount: Int
    var timestamp = Date()
    var languageCode: String
    var isTargetReached: Bool

    var pacePerMinute: Double {
        durationSeconds > 0 ? Double(sessionCount) / Double(durationSeconds) * 60 : 0
    }
}

enum HistoryTimeFilter: CaseIterable, Identifiable {
    case allTime, today, thisWeek, thisMonth

    var id: Self { self }

    func includes(_ date: Date, calendar: Calendar = .current) -> Bool {
        switch self {
        case .allTime: return true
        case .today: return calendar.isDateInToday(date)
        case .thisWeek: return calendar.isDate(date, equalTo: Date(), toGranularity: .weekOfYear)
        case .thisMonth: return calendar.isDate(date, equalTo: Date(), toGranularity: .month)
        }
    }
}

func formatDuration(_ totalSeconds: Int) -> String {
    let hours = totalSeconds / 3600
    let minutes = (totalSeconds % 3600) / 60
    let seconds = totalSeconds % 60
    return hours > 0
        ? String(format: "%02d:%02d:%02d", hours, minutes, seconds)
        : String(format: "%02d:%02d", minutes, seconds)
}

extension Color {
    static let deepNavy = Color(red: 0x0A / 255, green: 0x19 / 255, blue: 0x2F / 255)
    static let midnightBlue = Color(red: 0x17 / 255, green: 0x2A / 255, blue: 0x45 / 255)
    static let softGold = Color(red: 0xD4 / 255, green: 0xAF / 255, blue: 0x37 / 255)
    static let offWhite = Color(red: 0xF8 / 255, green: 0xFA / 255, blue: 0xFC / 255)
    static let textMuted = Color(red: 0x64 / 255, green: 0x74 / 255, blue: 0x8B / 255)
    static let accentGreen = Color(red: 0x10 / 255, green: 0xB9 / 255, blue: 0x81 / 255)
}
