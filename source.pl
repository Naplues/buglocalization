
=pod
	该脚本处理Java项目的源代码文件。
	1. 从项目中提取出所有源代码文件
	2. 提取源码文件中的单词构建语料库，(调用lscp)
=cut


use strict;
use warnings;
use File::Path;
use File::Copy;
use lscp;

=pod
	获取源码文件列表
	projectPath: 项目所在路径
	return:      项目中的Java源文件列表
=cut
sub getFileList {
	my $basedir = $_[0];
	my $d;
	my @files = ();
	my @dirs = ($basedir);
	die "error $basedir: $!" unless(-d $basedir);    
	while(@dirs) {
		$d = $dirs[0];
		$d .= "/" unless($d=~/\/$/);
		opendir my $folder, $d || die "Can not open this directory";
		my @filelist = readdir $folder; 
		closedir $folder;
		my $f;
		foreach (@filelist) {
			$f = $d . $_;
			#need to remove . and ..
			if($_ eq "." || $_ eq "..") {
			  next;                      #print "ignore"."\n";
			}
			push(@dirs, $f) if(-d $f);
			if($_ =~/.java$/){
				push(@files,$f) if(-f $f);
			}
		}
	   shift @dirs;
	}

	return @files;
}

sub getNewName {
	my @oldFiles = ();
	foreach (@_) {
		push(@oldFiles, $_);
		$_ =~s/\//-/g;
	}
	return @oldFiles;
}

=pod
	提取文件并改名存储
	projectPath: 项目路径
	outPath: 提取目的路径
=cut
sub extractSourceFiles {
	(my $projectPath, my $outPath) = @_;
	my @newFiles = getFileList($projectPath);
	my @files = getNewName(@newFiles);

	mkpath $outPath;
	my $i = 0;
	my $len = @files;
	for ($i = 0; $i < $len; $i++) {
		copy($files[$i], "$outPath/$newFiles[$i]")||warn "Could not copy files :$!" ;
	}
	print "Extract total $i files to $outPath/.\n";
}

=pod
	制作语料库
	inPath: 源码文件输入路径
	outPath: 单词文件输出路径
=cut
sub makeCorpus {
	(my $inPath, my $outPath) = @_;

	print "Constructing corpus...\n";
	my $preprocessor = lscp->new;
	$preprocessor->setOption("logLevel", "error");
	$preprocessor->setOption("inPath", $inPath);
	$preprocessor->setOption("outPath", $outPath);

	$preprocessor->setOption("doLowerCase", 1);
	$preprocessor->setOption("doStemming", 1);
	$preprocessor->setOption("doTokenize", 1);
	$preprocessor->setOption("doRemovePunctuation", 1);
	$preprocessor->setOption("doStopwordsEnglish", 1);
	$preprocessor->setOption("doStopwordsKeywords", 1);
	$preprocessor->setOption("fileExtensions", "java");
	$preprocessor->preprocess();
	print "Corpus construction has finished!\n";
}


=pod
	提取语料库中的单词
	path 路径
=cut
sub extractTerms {
	(my $fileList, my $termsFile) = @_;
	#获取terms列表
	my @files = getFileList($fileList);
	my %terms;
	my $num=0;
	foreach my $fileName (@files) {
		open(DATA, '<', $fileName) or die 'Cannot create file!';
		my @lines = <DATA>;
		foreach my $w (@lines){
			$terms{$w} += 1;
			$num++;
		}
		close(DATA);
	}
	my @keys = keys %terms;
	my $len = @keys;
	print "$len/$num\n";
	
	#输出单词到文件中
	unlink $termsFile;
	open(my $filehandle, '>>', $termsFile);
	foreach (sort { $a cmp $b } @keys) {
		print $filehandle "$terms{$_}, ", $_;
	}
	close $filehandle or die 'Cannot open file $!';
	return %terms;
}


#提取源码文件
extractSourceFiles('Tomcat', 'source/origin');
#建立语料库
makeCorpus('source/origin', 'source/target');
#提取单词
#extractTerms('source/target', 'source/terms.csv');




#执行系统命令 调用system
#my $commitID = "ca34a30";
#system("cd tomcat");
#system("git checkout $commitID~1");