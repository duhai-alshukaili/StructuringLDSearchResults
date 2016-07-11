#!/bin/bash

datasets=("movies" "cities" "people")
predicates=("Entity" "Property" "EntityType"  "HasType"  "HasProperty" "SimProperty" "SimEntity" "SimEntityType")


for ds in "${datasets[@]}" 
do

	echo `date` >> experiment.log
	echo "running ont+baseline inference on $ds" >> experiment.log
	echo "" >> experiment.log

	# run baseline+ont inference
	./inferWithOnt data/$ds

	# pre-pare results for evaluation
	java -Xmx4096m -cp ./target/classes:`cat classpath.out`  \
	        com.wordpress.chapter10.evaluation.ResultPrinter data/$ds/output data/$ds/gt

	# print to the output script
	echo '-------------------' >> $ds.out
	echo 'Baseline+Ont' >> $ds.out

	for pred in "${predicates[@]}" 
	do
	
	  fileName=data/$ds/output/$pred\_result.csv
	  iterName=$ds\_$pred
	  	  
	  python auc.py $fileName $iterName >> $ds.out
	done

	# save the inferece files to here
	mkdir -p data/$ds/output/baseline+ont/11-4-2016/
	mv data/$ds/output/*.csv data/$ds/output/baseline+ont/11-4-2016/

    # ----------------------------------------------------------------------------------------------
	# generate feedback
	python generateFBCands5Mid.py data/$ds/output/baseline+ont/11-4-2016/ data/$ds/feedback/

	echo `date` >> experiment.log
	echo "running ont+baseline+feeback (5% mid) inference on $ds" >> experiment.log
	echo "" >> experiment.log

	./inferWithOnt data/$ds

	java -Xmx4096m -cp ./target/classes:`cat classpath.out`  \
	    com.wordpress.chapter10.evaluation.ResultPrinter data/$ds/output data/$ds/gt

	# print to the output script
	echo '-------------------' >> $ds.out
	echo 'Baseline + Ont + Feedback(5% Middle)' >> $ds.out

	for pred in "${predicates[@]}" 
	do
	
	  fileName=data/$ds/output/$pred\_result.csv
	  iterName=$ds\_$pred
	  	  
	  python auc.py $fileName $iterName >> $ds.out
	done

	# save the inferece files to here
	mkdir -p data/$ds/output/baseline+ont+feedback/11-4-2016_5mid/
	mv data/$ds/output/*.csv data/$ds/output/baseline+ont+feedback/11-4-2016_5mid/

	# save feedback files
	mkdir -p data/$ds/feedback/11-4-2016_5mid/
	mv data/$ds/feedback/*.csv data/$ds/feedback/11-4-2016_5mid/

	# ----------------------------------------------------------------------------------------------

	# generate feedback
	python generateFBCands5Top.py data/$ds/output/baseline+ont/11-4-2016/ data/$ds/feedback/

	echo `date` >> experiment.log
	echo "running ont+baseline+feeback (5% top) inference on $ds" >> experiment.log
	echo "" >> experiment.log

	# run baseline+ont+feedback inference with 5% mid
	./inferWithOnt data/$ds

	java -Xmx4096m -cp ./target/classes:`cat classpath.out`  \
	    com.wordpress.chapter10.evaluation.ResultPrinter data/$ds/output data/$ds/gt

	# print to the output script
	echo '-------------------' >> $ds.out
	echo 'Baseline + Ont + Feedback(5% Top)' >> $ds.out

	for pred in "${predicates[@]}" 
	do
	
	  fileName=data/$ds/output/$pred\_result.csv
	  iterName=$ds\_$pred
	  	  
	  python auc.py $fileName $iterName >> $ds.out
	done

	# save the inferece files to here
	mkdir -p data/$ds/output/baseline+ont+feedback/11-4-2016_5top/
	mv data/$ds/output/*.csv data/$ds/output/baseline+ont+feedback/11-4-2016_5top/

	# save feedback files
	mkdir -p data/$ds/feedback/11-4-2016_5top/
	mv data/$ds/feedback/*.csv data/$ds/feedback/11-4-2016_5top/

	# ----------------------------------------------------------------------------------------------

	# generate feedback
	python generateFBCands3Mid2Top.py data/$ds/output/baseline+ont/11-4-2016/ data/$ds/feedback/

	echo `date` >> experiment.log
	echo "running ont+baseline+feeback (3% mid 2% top) inference on $ds" >> experiment.log
	echo "" >> experiment.log

	./inferWithOnt data/$ds

	java -Xmx4096m -cp ./target/classes:`cat classpath.out`  \
	    com.wordpress.chapter10.evaluation.ResultPrinter data/$ds/output data/$ds/gt

	# print to the output script
	echo '-------------------' >> $ds.out
	echo 'Baseline + Ont + Feedback(3% Mid, 2% Top)' >> $ds.out

	for pred in "${predicates[@]}" 
	do
	
	  fileName=data/$ds/output/$pred\_result.csv
	  iterName=$ds\_$pred
	  	  
	  python auc.py $fileName $iterName >> $ds.out
	done

	# save the inferece files to here
	mkdir -p data/$ds/output/baseline+ont+feedback/11-4-2016_3mid2top/
	mv data/$ds/output/*.csv data/$ds/output/baseline+ont+feedback/11-4-2016_3mid2top/

	# save feedback files
	mkdir -p data/$ds/feedback/11-4-2016_3mid2top/
	mv data/$ds/feedback/*.csv data/$ds/feedback/11-4-2016_3mid2top/

	# ----------------------------------------------------------------------------------------------

	# generate feedback
	python generateFBCands2Mid3Top.py data/$ds/output/baseline+ont/11-4-2016/ data/$ds/feedback/

	echo `date` >> experiment.log
	echo "running ont+baseline+feeback (2% mid 3% top) inference on $ds" >> experiment.log
	echo "" >> experiment.log

	./inferWithOnt data/$ds

	java -Xmx4096m -cp ./target/classes:`cat classpath.out`  \
	    com.wordpress.chapter10.evaluation.ResultPrinter data/$ds/output data/$ds/gt

	# print to the output script
	echo '-------------------' >> $ds.out
	echo 'Baseline + Ont + Feedback(2% Mid, 3% Top)' >> $ds.out

	for pred in "${predicates[@]}" 
	do
	
	  fileName=data/$ds/output/$pred\_result.csv
	  iterName=$ds\_$pred
	  	  
	  python auc.py $fileName $iterName >> $ds.out
	done

	# save the inferece files to here
	mkdir -p data/$ds/output/baseline+ont+feedback/11-4-2016_2mid3top/
	mv data/$ds/output/*.csv data/$ds/output/baseline+ont+feedback/11-4-2016_2mid3top/

	# save feedback files
	mkdir -p data/$ds/feedback/11-4-2016_2mid3top/
	mv data/$ds/feedback/*.csv data/$ds/feedback/11-4-2016_2mid3top/

	# ----------------------------------------------------------------------------------------------
	# generate feedback
	python generateFBCands10Mid.py data/$ds/output/baseline+ont/11-4-2016/ data/$ds/feedback/

	echo `date` >> experiment.log
	echo "running ont+baseline+feeback (10% mid) inference on $ds" >> experiment.log
	echo "" >> experiment.log

	./inferWithOnt data/$ds

	java -Xmx4096m -cp ./target/classes:`cat classpath.out`  \
	    com.wordpress.chapter10.evaluation.ResultPrinter data/$ds/output data/$ds/gt

	# print to the output script
	echo '-------------------' >> $ds.out
	echo 'Baseline + Ont + Feedback(10% Middle)' >> $ds.out

	for pred in "${predicates[@]}" 
	do
	
	  fileName=data/$ds/output/$pred\_result.csv
	  iterName=$ds\_$pred
	  	  
	  python auc.py $fileName $iterName >> $ds.out
	done

	# save the inferece files to here
	mkdir -p data/$ds/output/baseline+ont+feedback/11-4-2016_10mid/
	mv data/$ds/output/*.csv data/$ds/output/baseline+ont+feedback/11-4-2016_10mid/

	# save feedback files
	mkdir -p data/$ds/feedback/11-4-2016_10mid/
	mv data/$ds/feedback/*.csv data/$ds/feedback/11-4-2016_10mid/

	# ----------------------------------------------------------------------------------------------
	# generate feedback
	python generateFBCands10Top.py data/$ds/output/baseline+ont/11-4-2016/ data/$ds/feedback/

	echo `date` >> experiment.log
	echo "running ont+baseline+feeback (10% top) inference on $ds" >> experiment.log
	echo "" >> experiment.log

	./inferWithOnt data/$ds

	java -Xmx4096m -cp ./target/classes:`cat classpath.out`  \
	    com.wordpress.chapter10.evaluation.ResultPrinter data/$ds/output data/$ds/gt

	# print to the output script
	echo '-------------------' >> $ds.out
	echo 'Baseline + Ont + Feedback(10% Top)' >> $ds.out

	for pred in "${predicates[@]}" 
	do
	
	  fileName=data/$ds/output/$pred\_result.csv
	  iterName=$ds\_$pred
	  	  
	  python auc.py $fileName $iterName >> $ds.out
	done

	# save the inferece files to here
	mkdir -p data/$ds/output/baseline+ont+feedback/11-4-2016_10top/
	mv data/$ds/output/*.csv data/$ds/output/baseline+ont+feedback/11-4-2016_10top/

	# save feedback files
	mkdir -p data/$ds/feedback/11-4-2016_10top/
	mv data/$ds/feedback/*.csv data/$ds/feedback/11-4-2016_10top/

	# ----------------------------------------------------------------------------------------------
	# generate feedback
	python generateFBCands6Mid4Top.py data/$ds/output/baseline+ont/11-4-2016/ data/$ds/feedback/

	echo `date` >> experiment.log
	echo "running ont+baseline+feeback (6% mid, 4% top) inference on $ds" >> experiment.log
	echo "" >> experiment.log

	./inferWithOnt data/$ds

	java -Xmx4096m -cp ./target/classes:`cat classpath.out`  \
	    com.wordpress.chapter10.evaluation.ResultPrinter data/$ds/output data/$ds/gt

	# print to the output script
	echo '-------------------' >> $ds.out
	echo 'Baseline + Ont + Feedback(6% Mid, 4% Top)' >> $ds.out

	for pred in "${predicates[@]}" 
	do
	
	  fileName=data/$ds/output/$pred\_result.csv
	  iterName=$ds\_$pred
	  	  
	  python auc.py $fileName $iterName >> $ds.out
	done

	# save the inferece files to here
	mkdir -p data/$ds/output/baseline+ont+feedback/11-4-2016_6mid4top/
	mv data/$ds/output/*.csv data/$ds/output/baseline+ont+feedback/11-4-2016_6mid4top/

	# save feedback files
	mkdir -p data/$ds/feedback/11-4-2016_6mid4top/
	mv data/$ds/feedback/*.csv data/$ds/feedback/11-4-2016_6mid4top/

	# ----------------------------------------------------------------------------------------------
	# generate feedback
	python generateFBCands4Mid6Top.py data/$ds/output/baseline+ont/11-4-2016/ data/$ds/feedback/

	echo `date` >> experiment.log
	echo "running ont+baseline+feeback (4% mid, 6% top) inference on $ds" >> experiment.log
	echo "" >> experiment.log

	./inferWithOnt data/$ds

	java -Xmx4096m -cp ./target/classes:`cat classpath.out`  \
	    com.wordpress.chapter10.evaluation.ResultPrinter data/$ds/output data/$ds/gt

	# print to the output script
	echo '-------------------' >> $ds.out
	echo 'Baseline + Ont + Feedback(2% Mid, 3% Top)' >> $ds.out

	for pred in "${predicates[@]}" 
	do
	
	  fileName=data/$ds/output/$pred\_result.csv
	  iterName=$ds\_$pred
	  	  
	  python auc.py $fileName $iterName >> $ds.out
	done

	# save the inferece files to here
	mkdir -p data/$ds/output/baseline+ont+feedback/11-4-2016_4mid6top/
	mv data/$ds/output/*.csv data/$ds/output/baseline+ont+feedback/11-4-2016_4mid6top/

	# save feedback files
	mkdir -p data/$ds/feedback/11-4-2016_4mid6top/
	mv data/$ds/feedback/*.csv data/$ds/feedback/11-4-2016_4mid6top/


done
