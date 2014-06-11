#!/mmscripts/MathematicaScript -script

extentMarkers = 5; (*breite der markierungen*)

(* TODO: Subtraktion des Dateioffsets bei Annotationsdateien *)

directory=$ScriptCommandLine[[2]];
Print["Lese Daten aus Verzeichnis:                                 "<>directory];
dataDruck = (Import[directory <> "/data.csv", "Data", "HeaderLines" -> 0]);
ruhedruckFile=$ScriptCommandLine[[3]];
ruhedruckFile2=$ScriptCommandLine[[4]];

Print["Vorhergesagter Ruhedruck befindet sich in der Datei:        "<>ruhedruckFile];
rd = Import[ruhedruckFile, "Data", "HeaderLines" -> 0][[1]];
ruhedruck=ToExpression[rd];
Print["Der Ruhedruck ist (Sample):                                 "<>rd];
rd2 = Import[ruhedruckFile2, "Data", "HeaderLines" -> 0][[1]];
ruhedruck2=ToExpression[rd2];
Print["Der Ruhedruck2 ist (Sample):                                "<>rd2];


schluck=$ScriptCommandLine[[5]];
Print["Die Ausgabedatei bekommt die ID                             "<>schluck];
Print["Achtung: Datei wird überschrieben, sofern existent!"];

ReadMarkers[base_] := (
   
   start1 = Import[base <> "/rdstart", "Data", "HeaderLines" -> 0][[1]];
   ende1 = Import[base <> "/rdend", "Data", "HeaderLines" -> 0][[1]];
   samplerate1 = Import[base <> "/samplerate", "Data", "HeaderLines" -> 0][[1]];
   start2 = StringSplit[start1, ":"];
   start3 = ToExpression[start2[[2]]] + ToExpression[start2[[1]]]*60;
   ende2 = StringSplit[ende1, ":"];
   ende3 = ToExpression[ende2[[2]]] + ToExpression[ende2[[1]]]*60;
   Return[{start3*ToExpression[samplerate1], ende3*ToExpression[samplerate1]}];   
   );

markers = ReadMarkers[directory];
startTraining = markers[[1]] - dataDruck[[1, 1]];
Print["Trainingsdaten beginnen bei Sample:                         "<>ToString[startTraining]];
endTraining = markers[[2]] - dataDruck[[1, 1]];
Print["Trainingsdaten enden bei Sample:                            "<>ToString[endTraining]];


startIdx = 
  ToExpression[
    StringTake[(Import[directory <> "/channelstart", "Data", 
        "HeaderLines" -> 0]), {2}][[1]]] + 1;
endIdx = ToExpression[
    StringTake[
     ToString[endIdxRawString], {2, 
      Length[Characters[ToString[endIdxRawString]]]}]] + 1;

Print["Erstelle Datenplot..."];
daten = ListContourPlot[
   Log[dataDruck[[All, 2 ;; 20]] + 100 // Transpose], 
   PlotRange -> All, ContourLines -> False, 
   Contours -> ControlActive[2, 10], PlotLabel -> "Schluckdiagramm"];

Print["Markierungen..."];
marker = Graphics[{Red, 
    Rectangle[{ruhedruck - extentMarkers, 
      0}, {ruhedruck + extentMarkers, 1}]}];

marker2 = Graphics[{Blue, 
    Rectangle[{ruhedruck2 - extentMarkers, 
      0}, {ruhedruck2 + extentMarkers, 1}]}];


markerTrainLeft = 
  Graphics[{Green, 
    Rectangle[{startTraining - extentMarkers, 
      0}, {startTraining + extentMarkers, 1}]}];
markerTrainRight = 
  Graphics[{Green, 
    Rectangle[{endTraining - extentMarkers, 
      0}, {endTraining + extentMarkers, 1}]}];
bild1 = Show[{daten, marker, marker2, markerTrainLeft, markerTrainRight}];

Print["Schreibe Dateien..."];
Export["drucksensoren-" <> schluck <> ".png", bild1]

(* Export["c:/work/fft-" <> schluck <> ".png", bild2]
# Export["c:/work/maxima-" <> schluck <> ".png", bild3]
# Export["c:/work/smooth-" <> schluck <> ".png", bild4]*)

Print["Fertig!"];

