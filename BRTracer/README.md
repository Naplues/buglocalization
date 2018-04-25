# BRTracer

BRTracer is a new approach to bug-report-oriented fault localization.

To deal with post-release bugs, many software projects set up public bug repositories for users all over the world to report bugs that they have encountered. Recently, researchers proposed various information retrieval based approaches to localizing faults based on bug reports. Among these approaches, BugLocator, which relies on a revised Vector Space Model (rVSM), can outperform the other approaches. rVSM favors larger source code files that should be more likely to contain faults. However, as larger files may also be more likely to contain noise (less relevant but accidentally matched words), favoring larger files would tend to magnify the noise as well. Furthermore, bug reports often contain stack-trace information, which may include names of possible faulty files. Existing approaches always treat the bug descriptions as plain texts and do not explicitly consider stack-trace information. In this paper, we propose to use segmentation and stack-trace analysis to improve the performance for bug localization. Specifically, given a bug report, we divide each source code file into a series of segments and use the segment most similar to the bug report to represent the file. We also analyze the bug report to identify possible faulty files in a stack trace and favor these files in our retrieval. According to our empirical results, our approach is able to significantly outperform BugLocator on all the three software projects (i.e., Eclipse, AspectJ, and SWT) used in our empirical evaluation. Furthermore, segmentation and stack-trace analysis are complementary to each other for boosting the performance of bug-report-oriented fault localization.

# Contributions

We make the following main contributions in BRTracer:

* A novel approach based on segmentation and stack-trace analysis for bug-report-oriented fault localization.
* An empirical evaluation using three non-trivial projects (i.e., Eclipse, AspectJ and SWT) together with 3459 bug reports, demonstrating the superiority of our approach over BugLocator, which is by far the most advanced approach.