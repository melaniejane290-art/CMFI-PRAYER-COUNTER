import SwiftUI

@main
struct PrayerCounterApp: App {
    @StateObject private var store: PrayerStore

    init() {
        #if DEBUG
        if let code = UserDefaults.standard.string(forKey: "demoLang"), let language = AppLanguage(rawValue: code) {
            _store = StateObject(wrappedValue: PrayerStore.demo(language: language))
            return
        }
        #endif
        _store = StateObject(wrappedValue: PrayerStore())
    }

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
        #if DEBUG
        .onAppear {
            switch UserDefaults.standard.string(forKey: "demoScreen") {
            case "saved": selectedTab = 1
            case "history": selectedTab = 2
            default: selectedTab = 0
            }
        }
        #endif
    }
}
