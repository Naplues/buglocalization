
use strict;
use warnings;
use XML::LibXML;


#导出bug报告文件
sub exportBugReport {
	(my $projectName, my $xmlName) = @_;
	my $reportDirName  = $projectName. "\\BugReportFiles";  #bug报告内容文件
	my $bugFileDirName = $projectName. "\\BugLinkFiles";    #bug-file连接文件

	mkpath $reportDirName;
	mkpath $bugFileDirName;

	#加载xml信息
	my $dom = XML::LibXML->load_xml(location => $xmlName);

	my $i = 0;
	foreach my $bugs ($dom->findnodes('/pma_xml_export/database/table')) {
		my $bugID = $bugs->findvalue('./column[@name="bug_id"]');

		my $filename = "$reportDirName/$bugID.txt";
		open(my $filehandle, '>', $filename) or die 'Cannot create file!';
		#print $filehandle  $bugID, "\n";
		print $filehandle $bugs->findvalue('./column[@name="summary"]'), "\n";
		print $filehandle $bugs->findvalue('./column[@name="description"]'), "\n";
		close $filehandle or die "Cannot close the file handle\n";

		#bug fixed files
		$filename = "$bugFileDirName/$bugID.txt";
		open($filehandle, '>', $filename) or die 'Cannot create file!';
		print $filehandle $bugs->findvalue('./column[@name="files"]'), "\n";
		close $filehandle or die "Cannot close the file handle\n";

=pod
		#bug信息文件
		$filename = "$bugFileDirName/BugReportInfoFile.txt";
		if ($i == 0) {
			open($filehandle, '>', $filename) or die 'Cannot create file!';
		}else{
			open($filehandle, '>>', $filename) or die 'Cannot create file!';
		}
		print $filehandle $bugID, " ";
		print $filehandle $bugs->findvalue('./column[@name="commit"]'), " ";
		print $filehandle $bugs->findvalue('./column[@name="report_timestamp"]'), "\n";
		close $filehandle or die "Cannot close the file handle\n";
=cut
		$i++;
		print "Export $i bugs.\n";
	}

	print "Output Successfully!\n";
}

#提取Query
sub getQuery {
	my $projectName   = $_[0];
	my $reportDirName = $projectName. "\\BugReportFiles";  #bug报告内容文件
	my $queryDirName  = $projectName. "\\BugQueryFiles";   #bug查询文件

	my $preprocessor = lscp->new;
	$preprocessor->setOption("logLevel", "error");
	$preprocessor->setOption("inPath", $reportDirName);
	$preprocessor->setOption("outPath", $queryDirName);
	$preprocessor->setOption("isCode", 0);
	$preprocessor->setOption("doTokenize", 0);
	$preprocessor->setOption("doStemming", 1);
	# And any other options you wish to set

	$preprocessor->preprocess();
	print "Finish!\n";
}

#对bug报告按照时间进行排序
sub sortBugReportInfo {
	my $filename = $_[0];
	open(DATA, '<', $filename) or die 'Cannot create file!';
	my @lines = <DATA>;
	print $lines[0];

	close(DATA);
}

#exportBugReport($projectName, $xmlName);
#getQuery($projectName);

#sortBugReportInfo("$projectName/BugReportInfoFile.txt");