import SwiftUI

struct HistoryView: View {
    @EnvironmentObject private var store: PrayerStore
    @State private var filter = HistoryTimeFilter.allTime
    @State private var showClearConfirmation = false

    private var visible: [PrayerSessionRecord] {
        store.sessions.filter { filter.includes($0.timestamp) }
    }

    private func label(for filter: HistoryTimeFilter) -> String {
        switch filter {
        case .allTime: return store.text("All Time", "Tout")
        case .today: return store.text("Today", "Aujourd'hui")
        case .thisWeek: return store.text("This Week", "Cette semaine")
        case .thisMonth: return store.text("This Month", "Ce mois-ci")
        }
    }

    var body: some View {
        NavigationStack {
            Group {
                if store.sessions.isEmpty {
                    EmptyStateView(
                        systemImage: "clock",
                        title: store.text("No Prayer History Yet", "Aucun historique de prière"),
                        message: store.text(
                            "Start praying on the Counter tab and tap Save. Every prayer session will be logged here with timestamps, duration, and cadence.",
                            "Priez sur l'onglet Compteur puis appuyez sur Enregistrer. Chaque session sera archivée ici avec durée, horodatage et rythme."
                        )
                    )
                } else {
                    list
                }
            }
            .navigationTitle(store.text("History", "Historique"))
            .toolbar {
                if !store.sessions.isEmpty {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button(store.text("Clear", "Effacer"), role: .destructive) { showClearConfirmation = true }
                    }
                }
            }
            .confirmationDialog(
                store.text("Clear All Prayer History?", "Effacer tout l'historique ?"),
                isPresented: $showClearConfirmation,
                titleVisibility: .visible
            ) {
                Button(store.text("Clear History", "Effacer l'historique"), role: .destructive) { store.clearHistory() }
            } message: {
                Text(store.text("This action cannot be undone.", "Cette action est irréversible."))
            }
        }
    }

    private var list: some View {
        List {
            Section { summary }
            Section(store.text("Weekly Prayer Activity", "Activité de Prière Hebdomadaire")) { weeklyChart }
            Section {
                Picker("", selection: $filter) {
                    ForEach(HistoryTimeFilter.allCases) { Text(label(for: $0)).tag($0) }
                }
                .pickerStyle(.segmented)

                ForEach(visible) { session in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(session.topic).font(.headline).foregroundColor(.deepNavy)
                        Text("+\(session.sessionCount) · \(formatDuration(session.durationSeconds)) · \(String(format: "%.1f / min", session.pacePerMinute))")
                            .font(.subheadline)
                            .foregroundColor(.textMuted)
                        HStack {
                            Text(session.timestamp.formatted(date: .abbreviated, time: .shortened))
                                .font(.caption)
                                .foregroundColor(.textMuted)
                            Spacer()
                            ShareLink(item: store.shareText(for: session)) {
                                Label(store.text("Share Report", "Partager le bilan"), systemImage: "square.and.arrow.up")
                                    .labelStyle(.iconOnly)
                            }
                            .buttonStyle(.borderless)
                        }
                    }
                    .swipeActions {
                        Button(store.text("Delete", "Supprimer"), role: .destructive) {
                            store.deleteSession(session)
                        }
                    }
                }
            }
        }
    }

    private var summary: some View {
        HStack {
            metric("\(store.currentStreak())", store.text("Daily Streak", "Série Quotidienne"))
            Spacer()
            metric("\(store.sessions.count)", store.text("Sessions", "Sessions"))
            Spacer()
            metric(
                formatDuration(store.sessions.reduce(0) { $0 + $1.durationSeconds }),
                store.text("Total Time", "Temps total")
            )
        }
    }

    private func metric(_ value: String, _ title: String) -> some View {
        VStack(spacing: 2) {
            Text(value).font(.title3.weight(.bold)).foregroundColor(.deepNavy).monospacedDigit()
            Text(title).font(.caption2).foregroundColor(.textMuted)
        }
    }

    private var weeklyChart: some View {
        let days = store.lastSevenDays()
        let peak = max(days.map(\.count).max() ?? 0, 1)
        return HStack(alignment: .bottom, spacing: 8) {
            ForEach(days, id: \.day) { entry in
                VStack(spacing: 4) {
                    if entry.count > 0 {
                        Text("\(entry.count)").font(.caption2).foregroundColor(.textMuted).lineLimit(1).minimumScaleFactor(0.5)
                    }
                    RoundedRectangle(cornerRadius: 4)
                        .fill(Color.softGold)
                        .frame(height: max(4, 80 * CGFloat(entry.count) / CGFloat(peak)))
                    Text(entry.day.formatted(.dateTime.weekday(.narrow)))
                        .font(.caption2)
                        .foregroundColor(.textMuted)
                }
                .frame(maxWidth: .infinity)
            }
        }
        .frame(height: 120, alignment: .bottom)
    }
}
