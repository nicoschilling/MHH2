#!/bin/bash

function row {
	e "<div>"
	h2 "Schluckdiagramm $1"
	img "40%" "Proband-${2}-MJ-Schluck-$1.png" "Michaels Annotation"
	img "40%" "Proband-${2}-SM-Schluck-$1.png" "Simones Annotation"
	e "</div>"
	e '<hr style="clear:both"/>'
	
}

function row2 {
	e "<div>"
	h2 "Schluckdiagramm $1"
	img "40%" "Proband-${2}-MJ-Schluck-$1.png" "Michaels Annotation"
	#img "40%" "Proband-${2}-SM-Schluck-$1.png" "Simones Annotation"
	e "</div>"
	e '<hr style="clear:both"/>'
	
}

function img {
	e "<div style=\"width:$1;float:left\"><img style=\"width:100%\" src=\"$2\"/><br /><span>$3</span></div>"
}

function top {
	e "<html>"
}

function bottom {
	e "</html>"
}

function e {
	echo $1
}

function h1 {
	e "<h1>$1</h1>"
}

function h2 {
	e "<h2>$1</h2>"
}

function p {
	e "<p>$1</p>"
}

top

h1 Erläuterungen
p "Gegenübergestellt werden hier für jeden Schluck und jeden Probanden die individuellen Klassifikationen der Modelle (gelernt aus Michaels und Simones händischer Annotation). Das experimentelle Setting entspricht dem der allgemeinen Beschreibung (alle Schlücke außer dem Betrachteten dienen als Trainingsmenge)."
#p "<b>ACHTUNG:</b>Die sampleweisen Vorhersagen (Schluck / Nicht-Schluck) sind hier in einem Bereich von einer Sekunde geglättet. Durch diese Glättung scheint hier noch intern ein Zeichnungproblem in der benutzten Software vorzuliegen, sodass für einige Samples kein Durchschnittswert angezeigt wird. Dieses Problem ist nur visueller Natur."

h2 "Abbildungsbeschreibung"
p "Die Abbildungen enthalten 4 verschiedene Informationselemente:"
e "<ul>"
e "<li>Hintergrund: Das Schluckdiagramm"
e "<li>Grün:   Die Annotation des Ruhedruckbereichs"
e "<li>Gelb:   Die Maximaldruckkurve"
e "<li>Orange: Die sampleweise Klassifikation des Modells, ob es sich um 'Schluck' oder 'Nicht-Schluck' handelt."
e "<li>Rot:    Die Abschätzung des Modells, wann der Ruhedruck wieder erreicht ist."
e "<li>Blau:   Die händische Annotation, wann der Ruhedruck wieder erreicht ist."
e "</ul>"
p "Achtung: Die y-Skalen sind nicht einheitlich und dienen nur der Visualisierung. Interpretiert werden muss so: Ist der orange Wert auf 5, bedeutet dies 'Nicht-Schluck'. Ein Wert von 15 bedeutet 'Schluck'. Werte dazwischen eine relative Abstufung zueinander. Insbesondere ist hier die anschließende Glättung der individuellen vorhersagen der Samples entscheidend. "

h1 "Vergleich Proband 1"

for i in `seq 1 10`; do
	row $i "1"
done

h1 "Vergleich Proband 2"

for i in `seq 1 11`; do
	row $i "2"
done

h1 "Proband 3"

p "Die folgenden Vorhersagen sind auf Basis von Michaels Annotationen f&uuml;r Proband 1 getroffen."
p "ACHTUNG: Aufgrund der derzeitigen Implementierung der Methode konnten wir leider noch keine Vorhersagen f&uuml;r Schluck 11 und 12 machen. (Ist auf der TODO-Liste)"

for i in `seq 1 10`; do
	row2 $i "3"
done



bottom
