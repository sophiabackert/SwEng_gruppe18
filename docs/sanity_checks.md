# Sanity Checks

## Projekt ausführen

Das Projekt wurde mit folgenden Maven-Kommandos erfolgreich getestet:

```bash
mvn clean           # Löscht generierte Dateien
mvn compile         # Übersetzt den Quellcode
mvn javafx:run      # Startet die GUI
mvn test            # Führt Unit-Tests aus
```

## Erweiterte Sanity Checks

### JavaFX mit FXML (SceneBuilder)

Ziel: Testen, ob die FXML-Datei korrekt mit dem Controller verbunden ist.

**Vorgehen:**
- SceneBuilder installiert. (https://gluonhq.com/products/scene-builder/#download)
- Mit IntelliJ verknüpft.
- `main.fxml` mit SceneBuilder geöffnet.
- Einen Button eingefügt und mit einer Methode im zugehörigen Controller (`@FXML`) verknüpft.
- Projekt gestartet mit:
  ```bash
  mvn javafx:run
  ```
- Button-Klick triggert `System.out.println` → bestätigt, dass Verknüpfung funktioniert.

### JaCoCo & PMD / Maven Site Reports

Ziel: Testabdeckung, Codequalität und Duplikate prüfen.

**Befehl:**
```bash
mvn compile site
```

**Vorgehen:**
- Report unter `target/site/index.html` geöffnet.
- Alle Reports (JaCoCo, PMD, CPD, Surefire) überprüft.
- Sind alle erfolgreich erstellt wurden.

### JUnit 5 Testvalidierung

Ziel: Überprüfen, ob Tests korrekt ausgeführt und Fehler erkannt werden.

**Vorgehen:**
- Testdatei `ExampleTest.java` manipuliert:
  ```java
  @Test
  void testFails() {
      assertEquals(1, 2); // Muss fehlschlagen
  }
  ```
- Befehl:
  ```bash
  mvn test
  ```
- Fehler erkannt → Bestätigung, dass Test-Framework korrekt arbeitet.
- Danach Test wieder entfernt.

### jBox2D

Ziel: Grundlegende Integration und Funktionsweise der jBox2D-Bibliothek überprüfen.

**Vorgehen:**

- Welt und Beispielkörper zum Testen erstellt und einfache Kollisionen simuliert.

  (hier ist der Link zum gesamten Code [JBox2D Sanity Check](../src/main/java/mm/core/physics/JBox2D_Sanity_Check.java)):

  - Eine einfache Welt mit Schwerkraft (Vec2(0, -10)) erstellt.
  - Statischen Bodenkörper erzeugt.
  - Dynamischen Körper über dem Boden platziert.
  - 60 Schritte simuliert, um die Wirkung der Schwerkraft zu beobachten.
  - Position vor und nach der Simulation ausgegeben.
  
Projekt erfolgreich kompiliert und jBox2D-Logik durch einfache Konsolenausgabe validiert; Körper fällt.

### Git & GitLab Workflow

Ziel: Branching, Pushes und CI erfolgreich testen.

**Vorgehen:**
- Eigener `dev`-Branch erstellt und eingerichtet.
- Mehrfach Änderungen lokal vorgenommen und mit `git add`, `git commit` und `git push` in `origin/dev` gepusht.
- Merge Requests aus `dev` nach `master` vorbereitet bzw. initiiert.
- Konflikte (z. B. in der README.md) erfolgreich manuell gelöst und mit `git rebase --continue` abgeschlossen.
- GitLab CI durchlief die Pipelines nach jedem Push ohne Fehler.

**Erkenntnis:** Git-Setup, Branch-Strategie (`dev` → `master`) sowie GitLab CI funktionieren wie erwartet.