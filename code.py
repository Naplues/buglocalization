# -*- coding: UTF-8 -*-

import os
import random

#获取文件路径列表
def getFilePathList(fileDir, extension):
	L = []
	for root, dirs, files in os.walk(fileDir):
		for file in files:
			if os.path.splitext(file)[1] == extension:
				L.append(os.path.join(root, file))
	return L

def splitToBucket():
	path = 'D:/CrashLocator/result/firefox4.0b4/depth5/'
	lines =  open(path + 'expand.txt').readlines()
	for line in lines:
		newfile = './firefox4.0b4/' + line.split('|')[0] + '.txt'
		with open(newfile, 'a+') as f:
			f.write(line)
	print('Finish!')


#生成Sampling结果
def StackOnlySampling():
	for file in getFilePathList('./firefox4.0b4/', '.txt'):
		lines = open(file).readlines()
		#for x in range(100):
		line = lines[random.randint(0,len(lines)-1)]
		list = line.split('|')[2].split('\t')
		with open('SuspiciousFunctionList.txt', 'a+') as outfile:
			for i in range(len(list)):
				outfile.write(line.split('|')[0] + '\t' + list[i].split(':')[0] + '\t0.0\n')
		print(line.split('|')[0] + '\tFinish!')


if __name__ == '__main__':
	StackOnlySampling()