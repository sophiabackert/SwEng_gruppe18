# Benutzeranleitung - Mad Machines

## Inhaltsverzeichnis

1. [Einführung](#einführung)
2. [Spielstart](#spielstart)
3. [Hauptmenü](#hauptmenü)
4. [Level-Auswahl](#level-auswahl)
5. [Level-Editor](#level-editor)
6. [Spielmodus](#spielmodus)
7. [Spielobjekte](#spielobjekte)
8. [Steuerung](#steuerung)
9. [Spielmechaniken](#spielmechaniken)
10. [Tipps & Tricks](#tipps--tricks)
11. [Fehlerbehebung](#fehlerbehebung)
12. [Glossar](#glossar)

---

## Einführung

**Mad Machines** ist ein physikbasiertes Puzzle-Spiel, bei dem Sie durch geschickte Platzierung von Objekten einen Ball in die Zielzone bringen müssen. Das Spiel kombiniert strategisches Denken mit realistischem Physikverhalten.

### Spielprinzip
- **Ziel**: Bringen Sie den orangenen Spielball in die grüne Zielzone
- **Methode**: Platzieren Sie Objekte strategisch, um den Ball zu lenken
- **Herausforderung**: Nutzen Sie die Physik-Eigenschaften der Objekte optimal

---

## Spielstart

### Systemanforderungen
- **Betriebssystem**: Windows, macOS oder Linux
- **Java**: Version 11 oder höher
- **Speicher**: Mindestens 512 MB RAM
- **Bildschirm**: Mindestauflösung 1024x768

### Installation & Start
1. **Java installieren** (falls noch nicht vorhanden)
2. **Spiel starten**:
   ```bash
   # Über IDE: Main.java ausführen
   # Oder über Maven:
   mvn clean compile exec:java -Dexec.mainClass="mm.Main"
   ```

---

## Hauptmenü

Das Hauptmenü bietet folgende Optionen:

### Menüpunkte
- **Spiel starten**: Wechselt zur Level-Auswahl
- **Level-Editor**: Öffnet den Level-Editor
- **Einstellungen**: Konfiguration von FPS und SLOW MOTION für Fehler finden
- **Beenden**: Spiel beenden

---

## Level-Auswahl

### Verfügbare Level
- **Level 1-5**: Vorgefertigte Level mit steigendem Schwierigkeitsgrad
- **Mein Level**: Eigenes Level (falls vorhanden)

### Level-Informationen
Jedes Level zeigt:
- **Name**: Bezeichnung des Levels
- **Schwierigkeit**: EASY, MEDIUM, HARD
- **Ziel**: Beschreibung der Aufgabe
- **Vorschau**: Thumbnail des Levels

### Level starten
1. **Level auswählen** durch Klick
2. **"Spielen"** klicken
3. **Level-Editor öffnen** für Anpassungen

---

## Level-Editor

Der Level-Editor ermöglicht es Ihnen, eigene Level zu erstellen oder bestehende zu bearbeiten.

### Editor-Bereiche

#### **Inventar (Links)**
- **Verfügbare Objekte**: Alle Objekte, die Sie platzieren können
- **Anzahl-Limits**: Maximale Anzahl pro Objekttyp
- **Drag & Drop**: Objekte in den Editor ziehen

#### **Editor-Canvas (Mitte)**
- **Arbeitsbereich**: Hier platzieren Sie die Objekte
- **Vorgeladene Objekte**: Bereits vorhandene Level-Elemente
- **Platzierte Objekte**: Von Ihnen hinzugefügte Objekte

#### **Steuerung (Rechts)**
- **Aufgabe**: Beschreibung der Level-Zielsetzung
- **Aktionen**: Undo, Redo, Reset, Rotation

### Editor-Steuerung

#### **Objekt platzieren**
1. **Objekt aus Inventar ziehen**
2. **Auf gewünschte Position ziehen**
3. **Maus loslassen** zum Platzieren

#### **Objekt bearbeiten**
- **Linksklick**: Objekt auswählen
- **Rechtsklick**: Objekt löschen
- **Drag**: Objekt verschieben
- **Rotations-Buttons**: Objekt drehen

#### **Kollisionserkennung**
- **Rote Markierung**: Kollision mit anderem Objekt
- **Grüne Markierung**: Gültige Position
- **Automatische Warnung**: Bei ungültigen Platzierungen

### Editor-Funktionen

#### **Undo/Redo**
- **Undo**: Letzte Aktion rückgängig machen
- **Redo**: Rückgängig gemachte Aktion wiederholen
- **Verlauf**: Bis zu 20 Aktionen gespeichert

#### **Reset**
- **"Zurücksetzen"**: Entfernt alle platzierten Objekte
- **Vorgeladene Objekte bleiben**: Level-Grundstruktur bleibt erhalten

#### **Rotation**
- **Links drehen**: -10° Rotation
- **Rechts drehen**: +10° Rotation

### Testen & Speichern
- **"Spielen"**: Level testen
- **"Zurück"**: Zur Level-Auswahl
- **Automatisches Speichern**: Änderungen werden gespeichert

---

## Spielmodus

### Spielziel
Bringen Sie den **orangenen Spielball** in die **grüne Zielzone**.

### Spielablauf
1. **Level laden**: Alle Objekte werden positioniert
2. **Physik starten**: Objekte reagieren auf Schwerkraft
3. **Ball bewegen**: Durch Kollisionen und Physik
4. **Ziel erreichen**: Ball berührt die Zielzone

### Spielende
- **Erfolg**: Ball erreicht die Zielzone
- **Misserfolg**: Ball fällt aus dem Level
- **Neustart**: Level kann wiederholt werden

---

## Spielobjekte

### Bälle

#### **Spielball (Orange)**
- **Zweck**: Hauptobjekt, das ins Ziel gebracht werden muss
- **Physik**: Realistische Kollision und Rollverhalten
- **Eigenschaften**: Mittlere Dichte, gute Elastizität

#### **Bowlingball**
- **Zweck**: Schwerer Ball für Stöße und Gewicht
- **Physik**: Hohe Masse, geringe Elastizität
- **Verwendung**: Als Gewicht oder Stoßobjekt

#### **Tennisball**
- **Zweck**: Leichter, elastischer Ball
- **Physik**: Niedrige Masse, hohe Elastizität
- **Verwendung**: Für präzise Stöße und Sprünge

#### **Billiardball**
- **Zweck**: Präziser Ball für gezielte Stöße
- **Physik**: Mittlere Masse, gute Elastizität
- **Verwendung**: Für präzise Kollisionen

### Container & Hindernisse

#### **Eimer (Bucket)**
- **Zweck**: Fängt Bälle auf oder lenkt sie um
- **Physik**: Statisches Objekt, Kollisionserkennung
- **Verwendung**: Als Falle oder Umleitung

#### **Kiste (Cratebox)**
- **Zweck**: Schweres Hindernis oder Gewicht
- **Physik**: Hohe Masse, geringe Elastizität
- **Verwendung**: Als Barriere oder Gewicht

#### **Balken (Plank)**
- **Zweck**: Rampen, Brücken oder Hindernisse
- **Physik**: Rechteckige Form, verschiedene Winkel
- **Verwendung**: Für Rampen und Umleitungen

#### **Baumstamm (Log)**
- **Zweck**: Schweres, zylindrisches Objekt
- **Physik**: Rollt und kippt realistisch
- **Verwendung**: Als Gewicht oder rollendes Objekt

### Spezialobjekte

#### **Ballon**
- **Zweck**: Leichtes, schwimmendes Objekt
- **Physik**: Niedrige Masse, schwimmt nach oben
- **Verwendung**: Für leichte Stöße oder Auftrieb

#### **Domino**
- **Zweck**: Kettenreaktionen auslösen
- **Physik**: Fällt um und stößt andere an
- **Verwendung**: Für komplexe Kettenreaktionen

### Zonen

#### **Zielzone (Grün)**
- **Zweck**: Ziel des Spiels
- **Erkennung**: Automatische Erkennung bei Berührung
- **Aussehen**: Grüne, transparente Zone

#### **Einschränkungszone (Rot)**
- **Zweck**: Verbotener Bereich
- **Physik**: Kollisionen werden ignoriert
- **Verwendung**: Für Level-Begrenzungen

---

## Steuerung

### Maus-Steuerung

#### **Level-Editor**
- **Linksklick**: Objekt auswählen
- **Rechtsklick**: Objekt löschen
- **Drag & Drop**: Objekte verschieben
- **Scroll**: Zoom (falls verfügbar)

### Tastatur-Steuerung

#### **Allgemein**
- **ESC**: Zurück zum Hauptmenü
- **P**: Pause/Weiter
- **R**: Level neu starten
- **F11**: Vollbild umschalten

#### **Level-Editor**
- **Delete**: Ausgewähltes Objekt löschen
- **Strg+Z**: Undo
- **Strg+Y**: Redo
- **Pfeiltasten**: Objekt präzise verschieben

### Gamepad (falls unterstützt)
- **Linker Stick**: Kamera bewegen
- **Rechter Stick**: Zoom
- **A**: Bestätigen/Aktion
- **B**: Abbrechen/Zurück
- **X**: Objekt löschen
- **Y**: Rotation

---

## Spielmechaniken

### Physik-System

#### **Schwerkraft**
- **Richtung**: Nach unten (Y-Achse)
- **Stärke**: Realistische Erdbeschleunigung
- **Einfluss**: Auf alle nicht-statischen Objekte

#### **Kollisionen**
- **Erkennung**: Automatische Kollisionserkennung
- **Reaktion**: Realistische Abprall-Physik
- **Typen**: 
  - Kreis-Kreis
  - Kreis-Rechteck
  - Rechteck-Rechteck
  - Rotierte Objekte

#### **Material-Eigenschaften**
- **Dichte**: Bestimmt die Masse des Objekts
- **Reibung**: Beeinflusst das Rollverhalten
- **Elastizität**: Bestimmt die Abprall-Stärke

### Spielmechaniken

#### **Kollisionserkennung**
- **Überlappung**: Objekte können sich nicht überlappen
- **Warnungen**: Visuelle Hinweise bei Kollisionen
- **Präzision**: Pixelgenaue Erkennung

#### **Objekt-Interaktionen**
- **Stöße**: Objekte stoßen sich gegenseitig an
- **Stapelung**: Objekte können gestapelt werden
- **Kettenreaktionen**: Domino-Effekte möglich

#### **Ziel-Erkennung**
- **Berührung**: Ball muss Zielzone berühren
- **Automatisch**: Sofortige Erkennung
- **Feedback**: Visuelle und akustische Bestätigung

---

## Tipps & Tricks

### Allgemeine Strategien

#### **Level-Analyse**
1. **Ziel identifizieren**: Wo ist die Zielzone?
2. **Start-Position**: Wo beginnt der Ball?
3. **Hindernisse**: Was blockiert den Weg?
4. **Lösungsansatz**: Welche Objekte brauche ich?

#### **Objekt-Platzierung**
- **Stabilität**: Schwere Objekte unten platzieren
- **Präzision**: Leichte Objekte für feine Justierung
- **Balance**: Gleichgewicht zwischen Objekten beachten

### Spezifische Techniken

#### **Rampen bauen**
1. **Balken platzieren**: Als Rampe verwenden
2. **Winkel anpassen**: Für optimale Steigung
3. **Stabilisierung**: Schwere Objekte als Gewicht

#### **Fallen erstellen**
1. **Eimer positionieren**: Als Auffangbehälter
2. **Einlauf bauen**: Ball in Eimer lenken
3. **Auslauf**: Ball aus Eimer zur Zielzone

#### **Kettenreaktionen**
1. **Domino-Reihe**: Für automatische Abläufe
2. **Trigger-Objekt**: Erste Domino platzieren
3. **Zielkette**: Letzte Domino zur Zielzone

### Fortgeschrittene Techniken

#### **Physik ausnutzen**
- **Schwerkraft**: Bälle rollen bergab
- **Elastizität**: Bälle prallen ab
- **Masse**: Schwere Objekte bewegen leichte

#### **Präzisionsarbeit**
- **Kleine Anpassungen**: Millimeter-genaue Positionierung
- **Testen**: Häufig das Level testen
- **Optimierung**: Lösungen verfeinern

---

## Fehlerbehebung

### Häufige Probleme

#### **Spiel startet nicht**
- **Java prüfen**: Version 11 installiert?
- **Speicher**: Genügend RAM verfügbar?
- **Berechtigungen**: Ausführungsrechte vorhanden?
- **Maven**: Maven installiert für Build-Prozess?

#### **Level lädt nicht**
- **Datei-Integrität**: Level-Datei beschädigt?
- **Pfad**: Korrekte Datei-Struktur?
- **Format**: Gültiges JSON-Format?

#### **Physik-Probleme**
- **Performance**: Zu viele Objekte?
- **Kollisionen**: Ungültige Objekt-Positionen?
- **Stabilität**: Instabile Objekt-Konfigurationen?

### Lösungsansätze

#### **Performance-Optimierung**
- **Objekte reduzieren**: Weniger Objekte verwenden
- **Auflösung senken**: Grafik-Einstellungen anpassen
- **Hintergrund-Programme**: Andere Programme schließen

#### **Level-Reparatur**
- **Reset**: Level zurücksetzen
- **Neu erstellen**: Level von Grund auf neu bauen
- **Backup**: Vorherige Version wiederherstellen

---

## Glossar

### Spielbegriffe

#### **Level**
- **Definition**: Einzelne Spielrunde mit spezifischem Ziel
- **Komponenten**: Objekte, Zielzone, Physik-Regeln
- **Zweck**: Herausforderung für den Spieler

#### **Objekt**
- **Definition**: Spielbare Einheit mit Physik-Eigenschaften
- **Typen**: Bälle, Container, Hindernisse, Zonen
- **Verwendung**: Level-Gestaltung und Spielmechanik

#### **Physik**
- **Definition**: Realistische Simulation von Bewegungen
- **Elemente**: Schwerkraft, Kollisionen, Material-Eigenschaften
- **Zweck**: Realistische Spielmechanik

#### **Kollision**
- **Definition**: Berührung zwischen zwei Objekten
- **Erkennung**: Automatische Erkennung durch Engine
- **Reaktion**: Physik-basierte Abprall-Berechnung

#### **Zielzone**
- **Definition**: Grüne Zone, die das Level-Ziel markiert
- **Erkennung**: Automatische Erkennung bei Ball-Berührung
- **Zweck**: Spielziel definieren

### Technische Begriffe

#### **Canvas**
- **Definition**: Zeichenbereich für Level-Editor
- **Funktion**: Visuelle Darstellung und Interaktion
- **Verwendung**: Objekt-Platzierung und -Bearbeitung

#### **Inventar**
- **Definition**: Liste verfügbarer Objekte
- **Funktion**: Objekt-Auswahl und -Verwaltung
- **Einschränkungen**: Anzahl-Limits pro Objekttyp

#### **Undo/Redo**
- **Definition**: Rückgängig/Wiederholen von Aktionen
- **Funktion**: Änderungen verwalten
- **Limit**: Bis zu 20 Aktionen gespeichert

#### **Drag & Drop**
- **Definition**: Objekte per Maus verschieben
- **Funktion**: Intuitive Objekt-Platzierung
- **Validierung**: Kollisionsprüfung während des Ziehens

---

## Fazit

**Mad Machines** bietet eine einzigartige Kombination aus strategischem Denken und physikbasierter Spielmechanik. Mit dieser Anleitung haben Sie alle Werkzeuge, um das Spiel vollständig zu verstehen und zu genießen.

**Viel Spaß beim Spielen!**

