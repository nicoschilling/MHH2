#!/mmscripts/MathematicaScript -script

extentMarkers = 5; (*breite der markierungen*)
averagingWindow = 1;

(* TODO: Subtraktion des Dateioffsets bei Annotationsdateien *)

directory=$ScriptCommandLine[[2]];
Print["Lese Daten aus Verzeichnis:                                 "<>directory];
dataDruck = (Import[directory <> "/data.csv", "Data", "HeaderLines" -> 0]);
ruhedruckFile=$ScriptCommandLine[[3]];
ruhedruckFile2=$ScriptCommandLine[[4]];
predictionsFile=$ScriptCommandLine[[5]];
ausgabedatei=$ScriptCommandLine[[6]];


Print["Vorhergesagter Ruhedruck befindet sich in der Datei:        "<>ruhedruckFile];
rd = Import[ruhedruckFile, "Data", "HeaderLines" -> 0][[1]];
ruhedruck=ToExpression[rd];
Print["Der Ruhedruck ist (Sample):                                 "<>rd];
rd2 = Import[ruhedruckFile2, "Data", "HeaderLines" -> 0][[1]];
ruhedruck2=ToExpression[rd2];
Print["Der Ruhedruck2 ist (Sample):                                "<>rd2];

Print["Reading predictions file from                               "<>ToString[predictionsFile]];
predictions = Import[predictionsFile];

Print["Die Ausgabedatei heißt                                      "<>ausgabedatei];
Print["Achtung: Datei wird überschrieben, sofern existent!"];

start1 = Import[directory <> "/rdstart", "Data", "HeaderLines" -> 0][[1]];
ende1 = Import[directory <> "/rdend", "Data", "HeaderLines" -> 0][[1]];
channelstart = ToExpression[StringTake[(Import[directory <> "/channelstart", "Data", "HeaderLines" -> 0]), {2}][[1]]] + 1;
channelend = ToExpression[StringTake[(Import[directory <> "/channelend", "Data", "HeaderLines" -> 0]), {2,2}][[1]]] + 1;
samplerate = ToExpression[Import[directory <> "/samplerate", "Data", "HeaderLines" -> 0][[1]]];

start2 = StringSplit[start1, ":"];
start3 = (ToExpression[start2[[2]]] + ToExpression[start2[[1]]]*60)*samplerate;
ende2 = StringSplit[ende1, ":"];
ende3 = (ToExpression[ende2[[2]]] + ToExpression[ende2[[1]]]*60)*samplerate;

startTraining = start3 - dataDruck[[1, 1]];
Print["Trainingsdaten beginnen bei Sample:                         "<>ToString[startTraining]];
endTraining = ende3 - dataDruck[[1, 1]];
Print["Trainingsdaten enden bei Sample:                            "<>ToString[endTraining]];

Print["oÖS unterer Index:                                          "<>ToString[channelstart]];
Print["oÖS oberer Index:                                           "<>ToString[channelend]];

Print["Erstelle Datenplot..."];
daten = ListContourPlot[
   Log[dataDruck[[All, 2 ;; 20]] + 100 // Transpose], 
   PlotRange -> All, ContourLines -> False, 
   Contours -> ControlActive[2, 10], PlotLabel -> "Schluckdiagramm"];

Print["Erstelle Maximaldruckkurve ..."];
maxWindow = Table[Max[dataDruck[[i, channelstart ;; channelend]]], {i, 1, Length[dataDruck[[All, 1]]]}];
maximaldruckkurve = 
  ListPlot[((maxWindow/500)*15) + 3, PlotRange -> All, 
   PlotLabel -> "Maxima des Druckbereichs", 
   PlotStyle -> Directive[Yellow, Opacity[0.5], PointSize[Medium]]];



Print["Markierungen..."];
marker = Graphics[{Red, Rectangle[{ruhedruck - extentMarkers, 1}, {ruhedruck + extentMarkers, 19}]}];
marker2 = Graphics[{Blue, Rectangle[{ruhedruck2 - extentMarkers, 1}, {ruhedruck2 + extentMarkers, 19}]}];

markerAnnotation = Graphics[{Green, Rectangle[{annotation - extentMarkers, 1}, {annotation + extentMarkers, 19}]}];

markerTrainLeft = Graphics[{Green, Rectangle[{startTraining - extentMarkers, 1}, {startTraining + extentMarkers,19}]}];
markerTrainRight = Graphics[{Green, Rectangle[{endTraining - extentMarkers, 1}, {endTraining + extentMarkers, 19}]}];
predictions[[All,2]]=(predictions[[All,2]]+2)*5;
predictionsPlot = ListPlot[(predictions), PlotStyle -> Directive[Thick, Orange, Opacity[1.0],PointSize[Large]], Joined -> True, InterpolationOrder -> 1];

bild1 = Show[{daten, maximaldruckkurve, predictionsPlot, marker, marker2, markerTrainLeft, markerTrainRight}];

Print["Schreibe Dateien..."];
Export[ausgabedatei <> ".png", bild1]

Print["Fertig!"];

