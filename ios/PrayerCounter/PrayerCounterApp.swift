import SwiftUI

@main
struct PrayerCounterApp: App {
    @StateObject private var store = PrayerStore()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(store)
                .tint(.softGold)
        }
    }
}

struct RootView: View {
    @EnvironmentObject private var store: PrayerStore
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            CounterView()
                .tabItem { Label(store.text("Counter", "Compteur"), systemImage: "hands.sparkles.fill") }
                .tag(0)
            SavedView(onResume: { selectedTab = 0 })
                .tabItem { Label(store.text("Saved", "Proclamations"), systemImage: "bookmark.fill") }
                .tag(1)
            HistoryView()
                .tabItem { Label(store.text("History", "Historique"), systemImage: "clock.fill") }
                .tag(2)
        }
    }
}
