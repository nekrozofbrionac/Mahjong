


### Player View 
| Value             | Encoding       |
|-------------------|----------------|
| minFaan           | (HOE)          |
| prevailingWind    | Turn (HOE)     |
| playerSeat        | Turn (HOE)     |
| prevSeat          | Turn (HOE)     | 
| nextSeat          | Turn (HOE)     |
| oppoSeat          | Turn (HOE)     |
| turn              | Int            |
| wallReminaing     | Int            |
| currentTurnPlayer | Turn (HOE)     |
| prevMelds         | TileType[4][4] |
| nextMelds         | TileType[4][4] |
| oppoMelds         | TileType[4][4] |
| prevFlowers       | TileType[8]    |
| nextFlowers       | TileType[8]    |
| oppoFlowers       | TileType[8]    |
| discarded         | TileType[84]   |
| hand              | TileType[14]   |
| melds             | TileType[4][4] |
| flowers           | TileType[8]    |

### PlayerControl Actions
| Action              | Encoding |
|---------------------|----------|
| Winning             | Boolean  |
| kongFromDraw        | Boolean  |  
| kongFromDiscard     | Boolean  |
| pongFromDiscard     | Boolean  |
| chowFromDiscard     | TileType |
| (chow at all?)      | boolean  |

choosing a discard shall be hot one encoded, the highest one gets discarded
choosing a chow shall be floats from lowest to highest whereas those shall be encoded only by "yeah where would the tile go, as its lowest, in the middle or at the highest"


