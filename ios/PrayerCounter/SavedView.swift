import SwiftUI

struct SavedView: View {
    @EnvironmentObject private var store: PrayerStore
    let onResume: () -> Void

    @State private var searchText = ""
    @State private var completedOnly = false
    @State private var editing: SavedProclamation?

    private var visible: [SavedProclamation] {
        store.proclamations.filter { item in
            (!completedOnly || item.isTargetCompleted)
                && (searchText.isEmpty || item.topic.localizedCaseInsensitiveContains(searchText))
        }
    }

    var body: some View {
        NavigationStack {
            Group {
                if store.proclamations.isEmpty {
                    EmptyStateView(
                        systemImage: "bookmark",
                        title: store.text("No Saved Proclamations Yet", "Aucune proclamation enregistrée"),
                        message: store.text(
                            "Complete prayer sessions on the Counter tab and tap Save to record your spiritual proclamations here.",
                            "Effectuez des temps de prière sur le compteur et appuyez sur Enregistrer pour archiver vos proclamations ici."
                        )
                    )
                } else {
                    list
                }
            }
            .navigationTitle(store.text("Saved Proclamations", "Proclamations"))
            .sheet(item: $editing) { NotesEditor(proclamation: $0) }
        }
    }

    private var list: some View {
        List {
            Picker("", selection: $completedOnly) {
                Text(store.text("All", "Toutes")).tag(false)
                Text(store.text("Completed Target", "Objectif atteint")).tag(true)
            }
            .pickerStyle(.segmented)

            ForEach(visible) { item in
                VStack(alignment: .leading, spacing: 6) {
                    Text(item.topic).font(.headline).foregroundColor(.deepNavy)
                    Text("\(item.proclamationCount) / \(item.targetCount) · \(formatDuration(item.durationSeconds))")
                        .font(.subheadline)
                        .foregroundColor(.textMuted)
                    Text(item.timestamp.formatted(date: .abbreviated, time: .shortened))
                        .font(.caption)
                        .foregroundColor(.textMuted)
                    if !item.notes.isEmpty {
                        Text(item.notes).font(.footnote).italic()
                    }
                    HStack {
                        Button(store.text("Resume in Counter", "Reprendre dans le compteur")) {
                            store.resume(item)
                            onResume()
                        }
                        .buttonStyle(.borderless)
                        Spacer()
                        Button(store.text("Add Note", "Ajouter une note")) { editing = item }
                            .buttonStyle(.borderless)
                    }
                    .font(.footnote.weight(.semibold))
                }
                .swipeActions {
                    Button(store.text("Delete", "Supprimer"), role: .destructive) {
                        store.deleteProclamation(item)
                    }
                }
            }
        }
        .searchable(text: $searchText, prompt: store.text("Search proclamations by topic…", "Rechercher par sujet de proclamation…"))
    }
}

private struct NotesEditor: View {
    @EnvironmentObject private var store: PrayerStore
    @Environment(\.dismiss) private var dismiss
    let proclamation: SavedProclamation
    @State private var draft = ""

    var body: some View {
        NavigationStack {
            Form {
                TextField(
                    store.text("Add reflections, scripture references, or testimonies…", "Ajouter des réflexions, versets bibliques ou témoignages…"),
                    text: $draft,
                    axis: .vertical
                )
                .lineLimit(5...12)
            }
            .navigationTitle(store.text("Notes", "Notes"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(store.text("Cancel", "Annuler")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(store.text("Save", "Enregistrer")) {
                        store.updateNotes(of: proclamation, to: draft)
                        dismiss()
                    }
                }
            }
            .onAppear { draft = proclamation.notes }
        }
    }
}

struct EmptyStateView: View {
    let systemImage: String
    let title: String
    let message: String

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: systemImage).font(.largeTitle).foregroundColor(.softGold)
            Text(title).font(.headline).foregroundColor(.deepNavy)
            Text(message).font(.subheadline).foregroundColor(.textMuted).multilineTextAlignment(.center)
        }
        .padding(32)
    }
}
