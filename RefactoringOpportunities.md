
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