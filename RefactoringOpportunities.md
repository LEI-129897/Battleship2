
| Local                          | Nome do cheiro no código                        | Nome da refabricação | Número de aluno |
| :----------------------------- | :----------------------------------------------- | :--------------------- | :--------------- |
| Carrack.java::Construtor       | Duplicate branch in 'switch' (Switch Statements) | Extract Method         | 129897           |
| Frigate.java::Construtor       | Duplicate branch in 'switch' (Switch Statements) | Extract Method         | 129897           |
| Ship.java::`tooCloseTo()`    | Unused assignment - pos (Dead Code)              | Add Guard Clause       | 129897           |
| Ship.java::`isInsideBoard()` | Constant condition - this != null (Dead Code)    | Safe Delete            | 129897           |
| Ship.java::Construtor          | Redundant assignment (Dead Code)                 | Inline Variable        | 129897           |
| Game.java::`fireSingleShot()`  | Message Chains - !ship.stillFloating()           | Inline Method          | 129837           |
| Ship.java                      | Inappropriate Intimacy - protected List<IPosition> positions | Encapsulate Fields | 129837           |
| Move.java::`processEnemyFire()` | Long Method                                     | Extract Method         | 129837           |
| Galleon.java::`fillNorth()`    | Feature Envy - 'IPosition' accessed repeatedly   | Move Instance Method   | 129837           |
| GamePlaySession.java::`beginMove()` | Complex Condition - if (...) {}             | Introduce Variable     | 129837           |
| Position.java::`adjacentPositions()` | Repeated allocation of direction offsets      | Extract Constant       | 124790           |
| Tasks.java::`readClassicPosition()` | Unreachable duplicate parsing branch         | Remove Dead Code       | 124790           |
| Caravel.java::Construtor            | Duplicate branches in `switch`               | Consolidate Duplicate Branches | 124790 |
| Fleet.java::`getFloatingShips()` / `getSunkShips()` | Duplicate filtering loops | Parameterize Method | 124790 |
| Carrack.java / Frigate.java         | Duplicate position-generation methods        | Pull Up Method         | 124790           |
| Tasks.java::`menu()`                | Busy wait - `Thread.sleep()` inside loop (Qodana: BusyWait) | Replace with Scheduled Execution | 124790 |
| Game.java::`randomEnemyFire()`      | `Set.removeAll(List)` may be slow (Qodana: SlowAbstractSetRemoveAll, 2 occurrences) | Replace `removeAll` with iteration | 124790 |
| Ship.java::`getTopMostPos()`        | Suspicious indentation after `if` (Qodana: SuspiciousIndentAfterControlStatement) | Add Braces / Reformat Code | 124790 |
| Ship.java::`getRightMostPos()`      | Suspicious indentation after `if` (Qodana: SuspiciousIndentAfterControlStatement) | Add Braces / Reformat Code | 124790 |
| Game.java::`jsonShots()`            | Redundant null initializer (Qodana: UnusedAssignment) | Remove Redundant Initialization | 124790 |
| Position.java::'equals()' | Redundant Cast / Explicit instanceof check | Replace 'instanceof' with pattern variable | 129821 |
| Fleet.java::colisionRisk() |Uncommunicative Name (gralha no nome / parâmetro s)	| Rename |	129821|
| Compass.java::charToCompass()	| Verbose Switch Statement (atribuição repetida a variável local) | Replace 'switch' with 'switch' expression |	129821|
| Compass.java::charToCompass() |Redundant Local Variable (bearing)	| Inline Variable	|129821|
|Ship.java::getLeftMostPos()	|Indexed access to first element (get(0))	|Replace 'get(0)' with 'getFirst()'	| 129821|