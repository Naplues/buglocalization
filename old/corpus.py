# -*- coding: utf-8 -*-

import os
import re
import math
import numpy as np
import pickle as pkl
from nltk.tokenize import WordPunctTokenizer 
from porter2stemmer import Porter2Stemmer
#from nltk.stemmer.porter import PorterStemmer #词干提取

#获取文件路径列表
def getFilePathList(fileDir, extension):
	L = []
	for root, dirs, files in os.walk(fileDir):
		for file in files:
			if os.path.splitext(file)[1] == extension:
				L.append(os.path.join(root, file))
	return L

#从文件中提取单词
def getWordsFromFile(filePath):
	file = open(filePath, 'r')
	data = file.readlines()
	words = []

	for line in data:
		words.append(line)
	file.close()
	return words

#获取语料库
def getCorpus(filePath):
	filePathList = getFilePathList(filePath, '.java') #获取文件列表
	corpus = []
	i = 0
	for path in filePathList:
		words = getWordsFromFile(path)   #获取某文件中的单词
		corpus.append(words)
		i += 1
	return filePathList, corpus

#保存数据
def saveData(fileName, filePathList, corpus):
	output = open(fileName, 'wb')
	pkl.dump(filePathList, output, -1)
	pkl.dump(corpus, output, -1)
	output.close()
	print "Save data successfully!"

#读取数据
def loadData(fileName):
	pkl_file = open(fileName, 'rb')
	filePathList = pkl.load(pkl_file)
	corpus = pkl.load(pkl_file)
	pkl_file.close()
	print "Load data successfully!"
	return filePathList, corpus

#计算词频：单词在某个文件中的出现的频率
def getTF(corpus, bug_report_token):
	file_number = len(corpus)
	#bug报告向量
	bug_token_vector = np.mat(np.zeros(len(bug_report_token)))
	for j in range(len(bug_report_token)):      #bug报告中每个token
		count = 0                           #计算token在源文件中的数目
		for word in bug_report_token:
			if bug_report_token[j] == word:
				count += 1
		bug_token_vector[0,j] = count
		if bug_token_vector[0,j] > 0:
			bug_token_vector[0,j] = math.log(bug_token_vector[0,j]) + 1

	#文件权重矩阵
	file_weight_matrix = np.mat(np.zeros((file_number, len(bug_report_token))))  

	for i in range(file_number):                #处理语料库中的每个源文件
		for j in range(len(bug_report_token)):  #bug报告中每个token
			count = 0                           #计算token在源文件中的数目
			for word in corpus[i]:
				if bug_report_token[j] == word:
					count += 1
			file_weight_matrix[i,j] = count
			if file_weight_matrix[i,j] > 0:
				file_weight_matrix[i,j] = math.log(file_weight_matrix[i,j]) + 1
	return bug_token_vector, file_weight_matrix

#计算逆文件频率
def getIDF(corpus, bug_report_token):
	idf = []
	file_number = len(corpus)  #文件数目
	for i in range(len(bug_report_token)):
		count = 0
		for file in corpus:
			if bug_report_token[i] in file:
				count += 1
		if count == 0:
			idf.append(0)
		else:
			idf.append(file_number / count)
	return np.mat(idf)

#计算TF-IDF
def getTF_IDF(corpus, bug_report_token):
	tf_bug, tf_files = getTF(corpus, bug_report_token)
	idf = getIDF(corpus, bug_report_token)

	tfidf_files = np.multiply(tf_files, idf)
	tfidf_bug = np.multiply(tf_bug, idf)

	sim = []
	for file in tfidf_files:
		temp = np.dot(file, tfidf_bug.T)[0, 0]
		if math.sqrt(np.dot(file, file.T) * np.dot(tfidf_bug, tfidf_bug.T)) ==0:
			temp = 0;
		else:
			temp /= math.sqrt(np.dot(file, file.T) * np.dot(tfidf_bug, tfidf_bug.T))
		sim.append(temp)

	return np.argsort(-np.mat(sim))



if __name__ == "__main__":
	#获取语料库
	filePathList, corpus = getCorpus("./")
	#保存数据
	#saveData("corpus.pkl", filePathList, corpus)
	#读取数据
	#filePathList, corpus = loadData("corpus.pkl")

	'''
	dic = {}
	List = []
	for cor in corpus:
		for c in cor:
			dic[c] = 1
	'''
	
	BugQueryFiles = getFilePathList("Pro_Tomcat\\BugQueryFiles", '.txt')
	count = 0
	for path in BugQueryFiles:
		bug_report_token = getWordsFromFile(path)
		res = getTF_IDF(corpus, bug_report_token)

		f = open('Pro_Tomcat\\Result\\' + path[25:], 'w')
		for i in range(int(len(BugQueryFiles)/5)):
			f.write(filePathList[res[0, i]].replace('-', '/') + "\n")
		f.close()
		count += 1
		print count
	