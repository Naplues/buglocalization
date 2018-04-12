# buglocalization
Implement of bug localization<br>
query.pl<br>
	该脚本从数据库中提取bug查询信息<br>
	1. 提取每个bug的query,存储在query.txt中<br>
	2. 提取bug的索引,存储在index.txt中<br>
	3. 提取bug-link-file，存放在bugLinkFiles目录下<br>
<br>
source.pl<br>
	该脚本处理Java项目的源代码文件。<br>
	1. 从项目中提取出所有源代码文件<br>
	2. 提取源码文件中的单词构建语料库，(调用lscp)<br>
<br>
vsm.py<br>
	该脚本构建VSM模型来处理数据集<br>
<br>
<br>
result.py<br>
	对实验结果进行评估<br>
