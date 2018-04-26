# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###
1. raw_data: This is the directory storing raw data and ground truth data. 
             The sub-directory crash_stack_dir under each release version includes crash bucket data. Each sub-directory in crash_stack represents a crash bucket and includes crash reports under this bucket. 
			 The file oracle.txt includes the crash-inducing changes for crash buckets, as well as other information (such as the corresponding bug ID, bug fixing changes and so on).  			 
			 
2. analysis_data:  This is the directory storing the intermediate analysis results for crash data. Due to the storage limit in bitbucket, we provide the urls to download the analysis data (See analysis_data/analysis_results_url.txt). You can download the zip files from the provided urls. The downloaded zip file includes the static analysis results. After unzipping the zip file, there are 6 files in total. We also provided the running scripts that can facilitate to reproduce the static analysis results (e.g., analysis_data/6.5/6.5_analysis_script.sh). 
			 
			 **_cfg.txt file represents the control flow analysis results for each method. In this file, we index every method and assign a unique ID to each method. The control flow analysis and data flow analysis require this data. 
			 
			 **_variable_usage.txt represents the variable usage analysis results (including each variable's define, set, or use) in each method. 
			 
			 **_variable_usage_pro.txt: This file stores the processed result from **_variable_usage.txt. We index every variable and assign a unqiue ID to each variable. Note that, the method information is replaced by the unique ID of method which can be recovered from **_cfg.txt. The data flow analysis requires this data.  
			 
			 annotate.txt: This file stores mercurial annotation information. The annotation information records each source line and its introducing revision. 
			 
			 annotate_pro.txt: This file stores the processed annotation information for each method. We record the annotation information for each method, using the unique ID of each method. Note that, we can recover the method information based on the method ID and **_cfg.txt. The collection of candidate crash-induing changes requires this data. 

			 Form1.txt: This file stores the form 1 of candidate set of crash-induing changes. The form 1 of candidate set includes all the candidate changes from crash stacks.
			 
			 Form2.txt: This file stores the form 2 of candidate set of crash-induing changes. The form 2 of candidate set includes all the candidate changes from crash stacks excluding those unreachable lines via control flow analysis.
			 
			 Form3.txt: This file stores the form 3 of candidate set of crash-induing changes. The form 3 of candidate set includes all the candidate changes from crash stacks excluding those lines that has no influence on crash-related data via backward slicing.

3. prediction_data: This is the directory storing the features of each bucket. 
			In the folder under each version, there are three sub-directories corresponding to Form1 Form2 and Form3 respectively. 
			
			Under the folder of each form, threre are thw following three subfolders:
            
			training/train.csv: it is the training file used to train models and predict crash-inducing changes for the next version.
			
			testing/[BucketId].csv: it is the testing file for each bucket in the version. 
			
			features/[BucketId].csv: it stores the features of all the candidate instances for each bucket.
			 
4. tools: This is the directory storing the analysis tool, the tool ```changeLocator_predict.jar``` under the folder ```./tools/predict/```.

	**The required files for running this tool is as foolows:**
	```bucket_list.txt: which stores the information of all buckets ```

	```componentRevisions.txt: which stores component information for each revision ```

	```revision.log: which stores all the raw revisions for NetBeans project ```

	```revisionInfo.txt: which stores the information of all concerned revisions such as lines of codes, number of modified files```

	All these required files can be downloaded from ```./prediction_data/```

	Other required files are:

	```./raw_data/[Version]/oracle.txt``` which can be downloaded from ```./raw_data/```

	```./analysis/[Version]/{Form1,Form2,Form3}.txt```, which can be obtained after analyzing the data. We also provided it and can be downloaded from the link provided as described in the next section.

	```./analysis/[Version]/annotate_pro.txt```, which can be obtained after analyzing the data. We also provided it and can be downloaded from the link provided as described in the next section.

	***The parameters for running this tool:***

	```-t [task]```, which specifies the task to run. The tasks can be 	```obtainCandidates|featureExtraction|predict|all```, the parameter all runs all the three tasks.

	```-v [version]```, specifies the target version.

	```-f [Form]```, specifies the forms to run, which can be ```{Form1,Form2,Form3}```.

	```-p [version]```, only for tasks predict and all, specifies the training version.

	```-c [classifier]```, only for tasks predict and all, specifies the classifier, which can be 	```{Logistic|NaiveBayes|BayesNet|J48}```

### Steps to collect dataset ###
1. Download the Netbeans Crash Data. (Optional. We have provided the crash data in the directory raw_data.)

	(1) Query the crash buckets from http://statistics.netbeans.org/analytics/list.do?query
	
	(2) Given a crash bucket, we can query the crash reports from http://statistics.netbeans.org/analytics/detail.do?id=   with a specific bucket ID. 
	
	e.g., http://statistics.netbeans.org/analytics/detail.do?id=202526
	
	
	The data in raw_data directory was collected from this crash reporting system. 

2. Download the Netbeans Source Code. (Optional. We have provided the download links in the file analysis_data/source_code_download_urls.txt, so that you can directly download the source files of the selected versions without cloning the whole repository. Note that, cloning the whole repository of Netbeans may take quite a long time. )

	(1) Using the mercurial to clone the repository from https://hg.netbeans.org/releases

	(2) Download the specific version. 
			Suppose you have clone the repository in the directory "releases", use the command line and go into the directory "release".
			Then, use the command to download the targeted version. In our study, we use the following commands to download the corresponding versions.
			
				hg archive -r 76d51071af8a 6.5.zip
				
				hg archive -r 62da6e5b7962 6.7.zip
				
				hg archive -r ec9be6a6486c 6.8.zip
				
				hg archive -r cb6bcdf0c6ce 6.9.zip
				
				hg archive -r a693229ccdbb 7.0.zip
				
				hg archive -r e649e0c4c10c 7.1.zip
				
				hg archive -r b6c037585768 7.2.zip
			
3. Analyze the source code. (Optional. We provide the running script so that you can reproduce the analysis steps in the Linux system. The analysis script is analysis_data/$version$/$version$_analysis_script.sh, where $version$ is a version number of Netbeans. Note taht, you need to install the tool Understand before you can run the script. You can also skip the reproduce step and directly download from the urls provided in analysis_data/analysis_results_url.txt. For example, the analysis result of the Netbeans 6.5 is http://sccpu2.cse.ust.hk/wurongxin/download/6.5_analysis.zip ). 

	(1) Download the tool Understand from https://scitools.com/. Note that, Understand is a commercial software. You may need to apply for a trial version or buy a license. Install this tool. If you are in Linux platform, please add the directory where the binary file is into the environment variable $PATH. 

    (2) Unzip one version of Netbeans you download in the Step 2. Then we use the following commands to create a analysis database (Please make sure "und" is runnable after configuring the environment variable $PATH).
	
			und create -db 6.5.udb -languages java 
			
			#suppose you unzip the source code of netbeans 6.5 into the directory is 6.5_src
			
			und -db 6.5.udb add 6.5_src  
			
			und -db 6.5.udb analyze -all 
    
	(3) After createing the analysis database. Use the tool to extract control flow analysis and data flow analysis for every method (Please refer to the script analysis_data/$version/$version_analysis_script.sh  where $version is the specific version number of Netbeans).

    (4) We utilize the analysis result in Step(3) to analyze crash stacks in each crash bucket, and obtain the source lines and the corresponding inducing changes (Please refer to the script analysis_data/$version/$version_analysis_script.sh  where $version is the specific version number of Netbeans).

### Feature Extraction and Prediction ###
1. Obtaining candidates of inducing changes. (Optional. We have provied the candidates for each form of each version. For examle: ./prediction_data/7.2/Form3/candidates.txt)
We also provide the tool to extract such candidates, the tool is changeLocator_predict.jar under the directory ./tools/predict. You can use the following command to run it:

			java -jar changeLocator_predict.jar -t obtainCandidates -v 6.7 -f Form1 

2. Extract the features of crash-induing changes. (Optional. We have provided the features for crash-induing changes in the directory prediction_data. For example: ./prediction_data/6.5/Form2/features).
We also provide the tool to extract features, the tool is changeLocator_predict.jar under the directory ./tools/predict. You can use the following command to run it:

			java -jar changeLocator_predict.jar -t featureExtraction -v 6.7 -f Form1 
            
2. Use historical data to train a model and then rank the candidate crash-induing changes for new buckets. 
We provide the tool changeLocator_predict.jar under the directory ./tools/predict to do the prediction. You can use the following command to run it:

			java -jar changeLocator_predict.jar -t featureExtraction -p 6.5 -v 6.7 -f Form1 -c Logistic
Please make sure you have obtained the candidates and extracted the features before running the prediction.

We also provided the script to launch all these tasks for the three forms. Just run the script file for each version under the directory ```./tools/predict/predict_6.7```

If you have any question, please contact with wurongxin@cse.ust.hk