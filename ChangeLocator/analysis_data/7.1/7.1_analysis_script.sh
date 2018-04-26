# download the source code
wget http://sccpu2.cse.ust.hk/wurongxin/download/7.1_src.zip

#decompress the file
unzip 7.1_src.zip -d 7.1_src

#create analysis database
und create -db 7.1.udb -languages java

#add the source directory to be analyzed
und -db 7.1.udb add 7.1_src  

#begin to analyze the source files
und -db 7.1.udb analyze -all 

#get control flow information
java -Xmx4096m -cp ../../tools/stack_analyzer/StackAnalyzer.jar:../../tools/stack_analyzer/lib/libUnderstand.so collection.CFGCollection -db 7.1.udb -o 7.1_cfg.txt

#get data flow information
java -Xmx4096m -cp ../../tools/stack_analyzer/StackAnalyzer.jar:../../tools/stack_analyzer/lib/libUnderstand.so collection.VariableUsageCollection -db 7.1.udb -o 7.1_variable_usage.txt

# download the annotation file
wget http://sccpu2.cse.ust.hk/wurongxin/download/7.1_annotate.zip

#decompress the file
unzip 7.1_annotate.zip -d .

#preprocess the annotation information
java -Xmx4096m -cp ../../tools/stack_analyzer/StackAnalyzer.jar preprocess.PreprocessAnnotationFile2 -f 7.1_cfg.txt -a 7.1_annotate.txt -o annotate_pro.txt 

#preprocess the data flow information
java -Xmx8096m -cp ../../tools/stack_analyzer/StackAnalyzer.jar preprocess.PreprocessVariableUsage -f 7.1_cfg.txt -v 7.1_variable_usage.txt -o 7.1_variable_usage_pro.txt


#preprocess the crash bucket
java -Xmx4096m -cp ../../tools/stack_analyzer/StackAnalyzer.jar preprocess.PreprocessStack -f 7.1_cfg.txt -c ../../raw_data/7.1/crash_stack_dir -o processed_crash_data.txt

#analyze the crash stacks and obtain candidate set of crash-induing changes
#get form 1
java -Xmx4096m -cp ../../tools/stack_analyzer/StackAnalyzer.jar stack.expansion.StackAnalyzer -f 7.1_cfg.txt -vu 7.1_variable_usage_pro.txt -i processed_crash_data.txt -o Form1.txt

#get form 2
java -Xmx4096m -cp ../../tools/stack_analyzer/StackAnalyzer.jar stack.expansion.StackAnalyzer -f 7.1_cfg.txt -vu 7.1_variable_usage_pro.txt -i processed_crash_data.txt -o Form2.txt -cfa

#get form 3
java -Xmx4096m -cp ../../tools/stack_analyzer/StackAnalyzer.jar stack.expansion.StackAnalyzer -f 7.1_cfg.txt -vu 7.1_variable_usage_pro.txt -i processed_crash_data.txt -o Form3.txt -dfa


