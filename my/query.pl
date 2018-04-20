
=pod
	该脚本从数据库中提取bug查询信息
	1. 提取每个bug的query,存储在query.txt中
	2. 提取bug的索引,存储在index.txt中
	3. 提取bug-link-file，存放在bugLinkFiles目录下
=cut


use strict;
use warnings;
use File::Path;
use DBI;
use XML::LibXML;
use lscp;

#####################从数据库中提取信息###############################
=pod
	获取数据库句柄
	host:     主机名
	driver:   驱动
	database: 数据库
	userid:   用户名
	password: 密码
=cut
sub getDBHandle {
	(my $host, my $driver, my $database, my $userid, my $password) = @_;
	#驱动程序对象句柄
	my $dsn = "DBI:$driver:database=$database:$host";
	my $dbh = DBI->connect($dsn, $userid, $password) or die $DBI::errstr;
	return $dbh;
}

#按照顺序获取bug修复提交的commit
sub exportBugInfo {
	#连接数据库
	(my $dbh, my $sql) = @_;
	my $sth = $dbh->prepare($sql);  #预处理SQL语句
	$sth->execute();

	my $i = 0;
	mkpath 'bug_report';
	unlink glob './bug_report/*';
	open(my $fh1, '>>', 'bug_report/query.txt') or die 'Error';
	open(my $fh2, '>>', 'bug_report/index.txt') or die 'Error';

	#循环输出所有数据
	while(my @row = $sth->fetchrow_array()) {
		print $fh1 $row[2], "\n";
		print $fh2 $row[0], "\n";
		$i++;
		print "Export $i..\n";
	}
	close $fh1 or die "Cannot close the file handle\n";
	close $fh2 or die "Cannot close the file handle\n";

	$sth->finish();
	$dbh->disconnect();
	print "Export Bug Report Successfully!\n";
}

#导出和bug相关的文件
sub exportBugLinkFiles {
	my $xmlName = $_[0];
	my $bugFileDirName = "bug_report/BugLinkFiles";    #bug-file连接文件
	my $resultFileDirName = "bug_report/ResultLinkFiles";
	mkpath $bugFileDirName;
	mkpath $resultFileDirName;
	#加载xml信息
	my $dom = XML::LibXML->load_xml(location => $xmlName);
	my $i = 0;
	foreach my $bugs ($dom->findnodes('/pma_xml_export/database/table')) {
		my $bugID = $bugs->findvalue('./column[@name="bug_id"]');
		my $filename = "$bugFileDirName/$bugID.txt";
		open(my $filehandle, '>', $filename) or die 'Cannot create file!';
		print $filehandle $bugs->findvalue('./column[@name="files"]'), "\n";
		close $filehandle or die "Cannot close the file handle\n";
		
		$filename = "$resultFileDirName/$bugID.txt";
		open(my $fh3, '>', $filename) or die "Error";
		print $fh3 $bugs->findvalue('./column[@name="result"]'), "\n";
		close $fh3 or die "Cannot close the file handle\n";

		$i++;
		print "Export $i bugs link file(s).\n";
	}
	print "Export Bug Link Files Successfully!\n";
}

###############################################################
############################主程序#############################
###############################################################

my $projectName = "tomcat";

my $dbh = getDBHandle('localhost', 'mysql', $projectName, 'root', 'root');
my $sql = "SELECT bug_id, commit, bag_of_word_stemmed FROM bug_and_files ORDER BY report_time ASC";

exportBugInfo($dbh, $sql);
exportBugLinkFiles('Tomcat.xml')