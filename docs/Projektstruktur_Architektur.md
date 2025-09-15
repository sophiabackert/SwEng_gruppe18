# Architektur- und Struktur-Dokumentation

## 1. Grundsätzliche Architekturidee und Leitprinzipien

Die Architektur von Mad Machines ist konsequent auf Wartbarkeit, Erweiterbarkeit und Robustheit ausgelegt. Sie folgt einer klaren Schichtenarchitektur (Layered Architecture) und setzt auf die Trennung von Verantwortlichkeiten (Separation of Concerns). Jede Schicht ist so gestaltet, dass sie unabhängig von den anderen getestet und weiterentwickelt werden kann. Die Struktur orientiert sich an Elementen der Clean Architecture und Domain-Driven Design (DDD), wobei die tatsächliche Umsetzung stets pragmatisch an die Anforderungen des Projekts angepasst wurde.

**Zentrale Prinzipien:**
- **Modularisierung:** Jedes Modul (z.B. Physik, Rendering, Undo/Redo, Level-Validierung) ist als eigenständige Komponente realisiert und kann unabhängig weiterentwickelt werden.
- **Lose Kopplung:** Die Abhängigkeiten verlaufen immer nur von oben nach unten (GUI → Service → Domain), niemals umgekehrt. Dadurch bleibt die Kopplung gering und die Austauschbarkeit einzelner Komponenten hoch.
- **Hohe Kohäsion:** Funktional zusammengehörige Klassen sind in gemeinsamen Paketen gebündelt, z.B. alle Physik-bezogenen Klassen im Service-Physik-Modul.
- **Testbarkeit:** Die Trennung von GUI, Logik und Datenhaltung ermöglicht gezielte Unit- und Integrationstests, insbesondere für die Kernlogik.
- **Erweiterbarkeit:** Neue Features (z.B. neue Objekttypen, zusätzliche Level-Validierungen, weitere Editor-Funktionen) können mit minimalen Änderungen an bestehenden Klassen ergänzt werden.

---

## 2. Projektstruktur im Überblick

```
src/main/java/mm/
│
├── app/           // Einstiegspunkt, Hauptklasse
├── domain/        // Kernmodelle, Konfiguration, Validierung, Leveldaten
├── service/       // Spiellogik, Physik, Rendering, Undo/Redo, Kollisionen, Overlays
├── gui/           // Controller, Präsentationslogik, View-Management
└── (module-info.java)
```

### 2.1. App-Schicht
- **Paket:** `mm.app`
- **Klasse:** `Main`
- **Funktion:** Einstiegspunkt der Anwendung. Initialisiert die JavaFX-GUI, setzt den ViewManager auf und sorgt für die Übergabe der Kontrolle an die GUI-Schicht. Die App-Schicht kennt keine Details der Spiellogik oder Datenhaltung.

### 2.2. Domain-Schicht
- **Pakete:** `mm.domain.config`, `mm.domain.storage`, `mm.domain.editor`, `mm.domain.json`
- **Funktion:** Enthält alle Kernmodelle und Konfigurationen für Spielobjekte, Leveldaten und Validierungslogik. Diese Schicht ist komplett unabhängig von Frameworks und enthält keine GUI- oder Service-Logik.
  - **config:** Abstrakte und konkrete Konfigurationsklassen für alle Objekttypen (z.B. `TennisballConf`, `GoalZoneConf`). Die Basisklasse `ObjectConf` ist für Serialisierung und Validierung optimiert (Jackson-Annotationen, equals/hashCode, Subtyp-Handling).
  - **storage:** Leveldatenhaltung (`LevelData`), Schwierigkeitsgrade (`Difficulty`), und das Laden/Speichern von Leveln im JSON-Format (`LevelStorage`).
  - **editor:** Repräsentation platzierter Objekte im Editor (`PlacedObject`), inkl. Kopier- und Konvertierungslogik.
  - **json:** Validierung und Laden von Leveldateien (`LevelValidator`). Prüft alle Felder, Typen und Werte auf Korrektheit, bevor ein Level geladen wird.

### 2.3. Service-Schicht
- **Pakete:** `mm.service.command`, `mm.service.physics`, `mm.service.rendering`, `mm.service.object`, `mm.service.selection`, `mm.service.overlay`, `mm.service.collision`
- **Funktion:** Kapselt die Spiellogik und technische Funktionalität, die auf den Domänenmodellen operiert. Hierzu zählen:
  - **command:** Undo/Redo-Mechanismus (`CommandManager`) nach dem Command-Pattern. Unterstützt Add, Delete, Move, Rotate und begrenzt die Historie auf 20 Aktionen. Die Buttons werden automatisch aktiviert/deaktiviert.
  - **physics:** Physik-Engine (jBox2D) mit eigenem `PhysicsManager`. Kapselt die gesamte Physiksimulation, Kollisionserkennung, Siegbedingung (Ball in Zielzone), Weltgrenzen und Spezialeffekte (z.B. Ballon-Auftrieb). Die Physik ist vollständig von der GUI entkoppelt und kann separat getestet werden.
  - **rendering:** Rendering-Logik (`GameRenderer`) für die grafische Darstellung aller Objekte auf dem Canvas. Unterstützt Skins, Farbverläufe, Schatten und Glanzeffekte. Die RenderInfo-Struktur ist so gestaltet, dass sie flexibel für neue Objekttypen erweitert werden kann.
  - **object:** Verwaltung und Manipulation der Spielobjekte im Editor (`ObjectManager`). Unterstützt Drag & Drop, Limit-Prüfungen, Kollisionserkennung, PrePlaced-Objekte und das dynamische Nachladen von Skins.
  - **selection:** Auswahl- und Markierungslogik (`SelectionHelper`). Hebt selektierte Objekte hervor, steuert die Aktivierung der Rotationsbuttons und sorgt für konsistentes UI-Feedback.
  - **overlay:** Anzeige von Overlays und Warnungen (`OverlayHelper`). Ermöglicht temporäre, visuelle Rückmeldungen direkt im Editor.
  - **collision:** Kollisionserkennung und Überlappungsprüfung (`CollisionManager`). Unterstützt verschiedene Objektformen (Kreis, Rechteck, Bucket), Spezialregeln für Zonen und das Separating Axis Theorem für rotierte Rechtecke.

### 2.4. GUI-Schicht
- **Paket:** `mm.gui.controller`
- **Funktion:** Präsentationslogik und Steuerung der Benutzeroberfläche. Die Controller sind mit FXML-Views verbunden und vermitteln zwischen Benutzerinteraktion und Service-Schicht.
  - **Controller:** Abstrakte Basisklasse für alle Controller.
  - **GameController, GameEditorController, LevelEditorController, MainMenuController, SettingsController, LevelSelectionController, ViewManager:** Steuern die jeweiligen Ansichten und Interaktionen. Der `ViewManager` ist als Singleton ausgelegt und verwaltet das Laden und Wechseln aller Views, inklusive Weitergabe des Controllers.

---

## 3. Kommunikation zwischen den Komponenten

- **App → GUI:** Die Main-Klasse initialisiert die GUI und übergibt die Kontrolle an den ViewManager. Die App-Schicht kennt keine Details der Spiellogik.
- **GUI → Service:** Die Controller-Klassen rufen Methoden der Service-Klassen auf, um Spiellogik, Physik, Rendering, Undo/Redo, Objektverwaltung usw. zu steuern. Beispiel: Der LevelEditorController nutzt den ObjectManager für Drag & Drop, Undo/Redo und Kollisionen.
- **Service → Domain:** Die Service-Klassen arbeiten mit den Domänenmodellen, um Spielobjekte zu erzeugen, zu speichern, zu validieren und zu manipulieren. Beispiel: Der PhysicsManager erhält PlacedObjects, wandelt sie in ObjectConf um und fügt sie der Physikwelt hinzu.
- **Domain:** Die Domain-Schicht ist unabhängig und kennt keine anderen Schichten. Sie stellt reine Datenstrukturen und Validierungslogik bereit.

**Wichtige Aspekte:**
- Die Kommunikation erfolgt stets nur „nach unten“ (z.B. Controller → Service → Domain), niemals umgekehrt. Dadurch bleibt die Abhängigkeitshierarchie klar und die Kopplung gering.
- Die Service-Schicht ist so gestaltet, dass sie bei Bedarf auch von anderen Frontends (z.B. Headless-CLI) genutzt werden könnte.
- Die Validierung von Leveldaten erfolgt immer vor dem Laden, um Inkonsistenzen und Fehler frühzeitig abzufangen.

---

## 4. Erweiterbarkeit, Wartbarkeit und smarte Architekturentscheidungen

Die Architektur ist gezielt darauf ausgelegt, zukünftige Erweiterungen und Wartung zu erleichtern. Dies wird durch folgende Maßnahmen und Designentscheidungen erreicht:

- **Klare Trennung der Verantwortlichkeiten:** Jede Schicht und jedes Paket hat eine klar definierte Aufgabe. Beispiel: Die gesamte Undo/Redo-Logik ist im CommandManager gekapselt und kann unabhängig von der GUI getestet werden.
- **Lose Kopplung:** Die Komponenten sind so entkoppelt, dass Änderungen in einer Schicht möglichst wenig Auswirkungen auf andere Schichten haben. Beispiel: Die Physik-Engine kann durch eine andere ersetzt werden, ohne dass die GUI angepasst werden muss.
- **Hohe Kohäsion:** Ähnliche Funktionalitäten sind in gemeinsamen Paketen gebündelt. Beispiel: Alle Rendering-bezogenen Klassen befinden sich im Service-Rendering-Modul.
- **Einfache Testbarkeit:** Die Domain- und Service-Schichten können unabhängig von der GUI getestet werden. Beispiel: Die LevelValidator-Logik kann mit beliebigen JSON-Dateien getestet werden, ohne dass die GUI gestartet werden muss.
- **Einfache Erweiterung:** Neue Spiellogik, Objekttypen oder GUI-Elemente können durch Hinzufügen neuer Klassen in den jeweiligen Paketen realisiert werden, ohne bestehende Klassen stark zu verändern. Beispiel: Ein neuer Objekttyp benötigt nur eine neue Konfigurationsklasse, eine Erweiterung im ObjectManager und ggf. im Renderer.
- **Modularisierung:** Die Nutzung von Java-Modulen (module-info.java) sorgt für klare Sichtbarkeiten und Abhängigkeiten. Die Module können gezielt exportiert oder verborgen werden.
- **Validierung und Fehlerrobustheit:** Durch die strikte Validierung von Leveldaten und die defensive Programmierung in allen Service-Klassen werden Fehler frühzeitig erkannt und abgefangen.
- **GUI-Feedback und Usability:** Die OverlayHelper- und SelectionHelper-Klassen sorgen für ein konsistentes, direktes Feedback an die Nutzer:innen und erhöhen die Bedienbarkeit des Editors.
- **Reduktion und Modularität:** Aufgrund der Teamgröße wurden einige Wahlfeatures bewusst reduziert. Die Architektur bleibt jedoch so modular, dass spätere Erweiterungen (z.B. Headless-Mode, weitere Objekttypen, zusätzliche Editierfunktionen) problemlos möglich sind.
- **Skalierbarkeit:** Die Architektur ist so ausgelegt, dass auch größere Level, mehr Objekttypen und komplexere Spiellogik performant und übersichtlich abgebildet werden können.

---

## 5. Beispiel für eine Erweiterung

Um einen neuen Objekttyp (z.B. „Springfeder“) hinzuzufügen, sind folgende Schritte nötig:
1. Neue Konfigurationsklasse in `mm.domain.config` anlegen (z.B. `SpringfederConf`).
2. Rendering- und Physiklogik in den entsprechenden Service-Klassen ergänzen (`ObjectManager`, `GameRenderer`, ggf. `PhysicsManager`).
3. Anpassung der GUI-Controller, um das neue Objekt im Editor und Spiel verfügbar zu machen (z.B. Inventar-Item im LevelEditorController).
4. Optional: Validierungslogik im `LevelValidator` ergänzen.

Durch die klare Struktur ist sofort ersichtlich, wo welche Änderungen vorzunehmen sind. Bestehende Level und Funktionen bleiben dabei vollständig kompatibel.

---

## 6. Fazit

Die Architektur von Mad Machines ist gezielt darauf ausgelegt, Komplexität zu beherrschen, zukünftige Erweiterungen zu erleichtern und die Wartung des Codes übersichtlich und effizient zu gestalten. Die Trennung in App-, Domain-, Service- und GUI-Schicht bildet eine nachhaltige und professionelle Basis für die Weiterentwicklung des Projekts. Die getroffenen Architekturentscheidungen sind direkt an den Anforderungen und der tatsächlichen Umsetzung ausgerichtet und ermöglichen ein stabiles, flexibles und verständliches System. Die hohe Modularität, die konsequente Validierung und die klare Abgrenzung der Verantwortlichkeiten sorgen dafür, dass das Projekt auch langfristig wartbar und ausbaufähig bleibt. 