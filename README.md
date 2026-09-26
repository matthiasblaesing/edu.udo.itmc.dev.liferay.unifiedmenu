Unified-Menu
============

Generierung des vereinigten/unified Menüs. Die Idee hier:

Für angemeldete Benutzer sollten sowohl private, wie auch öffentliche Seiten
in einem Menü zur Verfügung stehen. Einträge mit gleicher friendly URL werden
dabei als "identisch" angesehen und ersetzen dabei öffentliche Seiten.

Manuelle Schritte für die Installation:

- in allen Seiten die bearbetiet werden sollen muss ein Menü mit dem Namen
  "UnifiedMenu" eingerichtet werden
- das Menü sollte als "Hauptnavigation" markiert werden, wenn das Menü gemäß
  dem Beispiel `src/main/site/conditional_menu_example.md` genutzt werden soll

Administrative Eintriffsmöglichkeiten:

- das Menü kann manuell neugebaut werden, durch manuelles Triggern des
  periodischen Rebuilds. Kontrollbereich -> Konfiguration -> Ausführung ->
  Geplante Aufgaben -> MenuRebuildJobConfiguration