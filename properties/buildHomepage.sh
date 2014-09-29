#!/bin/bash

function row {
	e "<div>"
	h2 "Schluckdiagramm $1"
	img "30%" "drucksensoren-$1.png" ""
	img "25%" "fft-$1.png" "Fourier-Spektrum"
	img "22%" "maxima-$1.png" "Maximaldruckkurve für Schluck $1"
	img "22%" "smooth-$1.png" "Bestimmung des Endzeitpunkts anhand erstem Unterschreitens des Durchschnittswertes des Ruhezeitraums"
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
p "Die Parameter sind nicht optimiert und entstammen den ersten 'Probeläufen' des ersten Patienten."

h1 "Proband 2"

for i in `seq 1 11`; do
	row $i
done

bottom
