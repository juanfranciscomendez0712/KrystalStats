# KrystalStats
Plugin para servidores Minecraft Paper, que cambia el motor de daño del juego por uno propio.

La manera de calcular la reducción de daño es la siguiente:

Primero, se calculan las estadisticas de defensa, armadura, dureza de armadura y encantamientos del jugador, bajo esta logica:
Cada punto de armadura (atributo armor vanilla), otorga +2.5 puntos de reducción, siendo el máximo 50.

Cada nivel del encantamiento de protección, otorga +1.5 puntos de reducción.

En caso que la pieza de armadura no tenga protección, pero si una protección especializada (ejemplo, contra proyectiles), entonces otorgará +2 puntos de reducción por nivel (en caso que el daño recibido sea de su tipo).

En caso que ya tenga encantamiento de protección, y sobre ese, se tiene otro tipo de protección (ejemplo, prote IV + Prote contra explosiones III), entonces la protección especializada solo dará +0.25 puntos de reducción (si se recibe el daño de su tipo) + los puntos del encantamiento protección.

Cada punto de reducción corresponde a 1% menos daño recibido, hasta un máximo de 90%.

Además, se obtiene la defensa, cada 1 de defensa corresponde a -1 de daño recibido.

Por ultimo en obtención de estadísticas, se obtiene la dureza de armadura total del jugador, para calcular más adelante la eficiencia de la reducción.

Ya que se han obtenido las estadisticas del objetivo, se utiliza la reducción por defensa primero, pese a que 1 de defensa es -1 de daño, por mas defensa que tenga el objetivo, siempre saldrá de este calculo como minimo 1 de daño, no menos.

Una vez pasa la primera etapa, llega a la segunda etapa, donde se utilizan los puntos de reducción.

se usa el daño luego de pasar por la defensa, y se calcula:
(puntos_de_reduccion + (dureza_de_armadura / 2.0)) / (puntos_de_reduccion + daño_despues_de_la_defensa).

esto es para obtener la eficiencia de la reducción según el daño que se reciba. Mientras más alto el daño, menos eficiente será la reducción.

entonces, se multiplica la reducción porcentual base de la armadura (que llega como maximo a 90%) con la eficiencia de la reducción, obteniendo la reducción real que se usará para el daño.

finalmente, cuando el daño pasa esta etapa, termina verificando si el jugador posee el efecto de poción resistencia. Si es así, aplica una reducción extra del 15% x nivel del efecto.
