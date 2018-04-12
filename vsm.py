# coding:utf-8
'''
	该脚本构建VSM模型来处理数据集

'''

import os
import numpy as np
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer 

#获取文件路径列表
def getFilePathList(fileDir, extension):
	L = []
	for root, dirs, files in os.walk(fileDir):
		for file in files:
			if os.path.splitext(file)[1] == extension:
				L.append(os.path.join(root, file))
	return L

#获取语料库
def getCorpus(fileDir):
	fileList = getFilePathList(fileDir, '.java')
	corpus = []
	for fileName in fileList:
		temp = ''
		file = open(fileName, 'r')
		for w in file.readlines():
			temp += w.replace('\n', ' ')
		corpus.append(temp)
	corpus.append('')
	return corpus

#获取bug查询
def getBugQuery(filePath):
	file = open(filePath + '/query.txt', 'r')
	query = file.readlines()
	file = open(filePath + '/index.txt', 'r')
	index = file.readlines()
	return query, index

#计算余弦相似度
def cosine(vector1, vector2):  
    dot_product = 0.0
    normA = 0.0
    normB = 0.0
    for a,b in zip(vector1,vector2):
        dot_product += a*b
        normA += a**2
        normB += b**2
    if normA == 0.0 or normB==0.0:
        return None
    else:
        return dot_product / ((normA*normB)**0.5)

#VSM模型
#	corpus: 语料库list
def VSM_Model(corpus, bug_query, index, other):
	corpus[-1] = bug_query
	#将文本中的词语转换为词频矩阵
	vectorizer = CountVectorizer()
	#计算每个词语出现的次数
	X = vectorizer.fit_transform(corpus)
	#获取词袋中所有文本关键词
	#print vectorizer.get_feature_names()
	#查看词频结果
	count = X.toarray()
	#查看单词的索引
	#print vectorizer.vocabulary_.get('type')
	transformer = TfidfTransformer(smooth_idf = False)
	tfidf = transformer.fit_transform(count)
	V = tfidf.toarray()
	sim = []
	for x in range(len(V)-1):
		sim.append(cosine(V[x], V[-1]))
	res = np.argsort(-np.mat(sim))
	filePathList = getFilePathList('source/target', '.java')
	path = 'result/' + index.replace('\n', '') + '.txt'
	f = open(path, 'w')
	for i in range(len(filePathList)):
		f.write(filePathList[res[0, i]].replace('-', '/').replace(other, '') + "\n")
	f.close()
	
if __name__ == "__main__":
	#语料库
	corpus = getCorpus('source/target')
	#查询
	query, index = getBugQuery('bug_report')
	for x in range(len(query)):
		print x
		VSM_Model(corpus, query[x], index[x], 'source/target\Tomcat/')