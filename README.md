# KrystalStats
Plugin para servidores Minecraft Paper, que cambia el motor de daño del juego por uno propio
La manera de calcular la reducción de daño es la siguiente:

Primero, se calculan las estadisticas de defensa, armadura, dureza de armadura y encantamientos del jugador, bajo esta logica:
Cada punto de armadura (atributo armor vanilla), otorga +2.5 puntos de reducción, siendo el máximo 50
Cada nivel del encantamiento de protección, otorga +1.5 puntos de reducción
En caso que la pieza de armadura no tenga protección, pero si una protección especializada (ejemplo, contra proyectiles), entonces otorgará +2 puntos de reducción por nivel (en caso que el daño recibido sea de su tipo)
En caso que ya tenga encantamiento de protección, y sobre ese, se tiene otro tipo de protección (ejemplo, prote IV + Prote contra explosiones III), entonces la protección especializada solo dará +0.25 puntos de reducción (si se recibe el daño de su tipo) + los puntos del encantamiento protección

Cada punto de reducción corresponde a 1% menos daño recibido, hasta un máximo de 90%

Además, se obtiene la defensa, cada 1 de defensa corresponde a -1 de daño recibido

Por ultimo, en obtención de estadísticas, se obtiene la dureza de armadura total del jugador, para calcular más adelante la eficiencia de la reducción
