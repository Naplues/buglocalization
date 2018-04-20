# -*- coding: utf-8 -*-
'''
	该脚本对实验结果进行评估

'''

import os

#获取文件路径列表
def getFilePathList(fileDir, extension):
	L = []
	for root, dirs, files in os.walk(fileDir):
		for file in files:
			if os.path.splitext(file)[1] == extension:
				L.append(os.path.join(root, file))
	return L

#逐行读取文件内容到list中
def getLinesFromFile(filePath):
	file = open(filePath, 'r')
	data = file.readlines()
	file.close()
	return data

#accuracy@k
def accuracy_k(topk):
	#实际序列文件路径
	a_path = getFilePathList('bug_report\\BugLinkFiles\\', '.txt')
	#预测序列文件路径
	p_path = getFilePathList('result', '.txt')

	summ = 0
	total = len(p_path)
	for x in range(total):
		result = 0
		a_data = getLinesFromFile(a_path[x])
		p_data = getLinesFromFile(p_path[x])
		#len(p_data)/100**topk
		for i in range(topk):
			if p_data[i] in a_data:
				result = 1
				break
		summ +=result
	print "Top[" + str(topk) + "] Precise: " + str(float(summ)/total)


#Top@k: 准确率
def getTop_K(k):
	for x in range(1,k + 1):
		accuracy_k(x)

#MAP: 平均准确度均值
def getMAP():
	#实际序列文件路径
	a_path = getFilePathList('bug_report\\BugLinkFiles\\', '.txt')
	#预测序列文件路径
	p_path = getFilePathList('result', '.txt')
	res = 0   #MAP结果
	count = 0 #有多少样例参与计算(position 长度>0)
	for x in range(len(a_path)):
		a_file = getLinesFromFile(a_path[x])
		p_file = getLinesFromFile(p_path[x])
		position = []
		for i in range(len(a_file)):
			for j in range(len(p_file)):
				if a_file[i] == p_file[j]:
					position.append(j+1)
		position.sort()
		if len(position) > 0:
			count += 1
			summ = 0
			for i in range(len(position)):
				summ += float(i + 1)/position[i]
			summ /= len(position)
			res += summ
	res /= count
	print "MAP: " + str(res)

#作者的结果
def res(k):
	#结果文件路径
	path = getFilePathList('bug_report\\ResultLinkFiles\\', '.txt')

	res = 0   #MAP结果
	count = 0 #有多少样例参与计算(position 长度>0)
	topk = 0
	for x in path:
		file = getLinesFromFile(x)
		position=[]
		for data in file:
			d = data.split(":")[0]
			if d.isdigit():
				position.append(int(d))
		position.sort()
		
		if len(position) > 0:
			for x in range(1, k+1):
				if x in position:
					topk += 1
		

		if len(position) > 0:
			count += 1
			summ = 0
			for i in range(len(position)):
				summ += float(i + 1)/position[i]
			summ /= len(position)
			res += summ
	res /= len(path)
	print "Top:[" + str(k) + "] " + str(float(topk)/len(path))
	print "MAP: " + str(res)

######################主程序: 度量方法效果#########################
if __name__ == "__main__":
	#getTop_K(20)
	#getMAP()
	res(20)