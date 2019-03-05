/* 
 * Copyright 2016 Lutz Fischer <l.fischer@ed.ac.uk>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rappsilber.utils;


/**
 *
 * @author Lutz Fischer <l.fischer@ed.ac.uk>
 */
public class XiVersion {
    public static final String m_revstring="$Rev: 746 $";
    public static final String m_extension="dev";
// overlap  
//    public static final int m_major = 1;
//    public static final int m_minor = 2;
//    public static int m_build = -1;
    public static Version version = new Version(1, 6, m_revstring,m_extension);
     
    
    public static final String changes = 
                                "Version 1.6.746\n" +
                                "  * Bugfix for NoDigestion throwing an NullPointerException\n" +
                                "  * New peak-list iterator that tries to read all fiels in a folder\n" +
                                "  * Use a wrapper for FileInputStream (RobustFileInputStream)  that tries to reopen the underlying file on Errors\n" +
                                "  * ZipStreamIterator - a zip-fiel access that uses the RobustFileInputStream to acces the zip-file\n" +
                                "  * Ensure soem buffers are flushed before closing a search-thread\n" +
                                "  * DB: All scores get written out as an array in spectrum_match\n" +
                                "  * DB2Pinpoint no longer supports pinpoint but only skyline\n" +
                                "  * changed to maven project\n" +
                                "  * Several BugFixes to get Open-and Targetd modifications up to speed with recent canges\n" +
                                "Version 1.6.745\n" +
                                "  * BugFix decoy proteins not assigned a source\n" +
                                "Version 1.6.744\n" +
                                "  * AminoAcidRestrictedImmoniumIons maybe usable\n" +
                                "  * some output try to finish cleaning on CTRL+C\n" +
                                "  * new sub score CCPepFragmentIntensity\n" +
                                "  * BugFix: bugfix RUN_RE and SCAN_RE was ignored for first spectrum\n" +
                                "  * BugFix: PeaklistWriter not closing down correctly\n" +
                                "  * Bugfix: Commandline xi now has a default local set (en)\n" +
                                "  * Bugfix: PeakListWriter did not properly close the output\n" +
                                "  * Bugfix: Cloned Spectra did not forward the info of additional \n"+
                                "       charges or m/z values to be considered\n" +
                                "  * Bugfix: spelling error for CleavableCrossLinkerPeptideFragment\n" +
                                "  * BugFix: if one peak has several lossy annotations to only one was\n" + 
                                "       removed if it when they did not fit to linkage site\n" +
                                "  * if losses are twice defined it will only ones be considered\n" +
                                "  * maybe BugFix: DecimalFormat output odd character for Double.NaN\n" +
                                "V1.6.743\n" +
                                " * new modification type - besides \"fixed\", \"variable\", and \"known\" one \n" +
                                "	can now define \"linear\" modification that are only to be found on \n" +
                                "	peptides matched as linear peptides \n" +
                                " * some scores get the RunConfig as part of the initialisation\n" +
                                " * renamed \"searchStoped\" to searchStopped in SimpleXiProcess\n" +
                                " * when threads stop they report the reason they stop\n" +
                                " * reordered some initialisations in AbstractRunConfig\n" +
                                " * storeObject and retrieveObject are caseinsensetive if a String is \n" +
                                "	used as key\n" +
                                " * updated filter readout from config\n" +
                                " * new Filter \"RemoveSinglePeaks\" as an alternative denoising method\n" +
                                " * new Filter \"ScanFilteredSpectrumAccess\" to only search specific scans\n" +
                                "	actually that filter already exists but was not selectable via the \n" +
                                "	config\n" +
                                " * new filter \"Rebase\": the main use is to write out the filtered \n" +
                                "	spectrum to the database by declaring the up to that point processed \n" +
                                "	spectrum as the \"original\" spectrum \n" +
                                " * GenericTextPopUpMenu insalles now also a simple search function on \n" +
                                "	textareas\n" +
                                " * the peakfile table the database is now filed and referenced\n" +
                                " * peakFileName will be recorded for each spectrum.\n" +
                                " * CSVExportMatches can change the delimiter and quote charater that is \n" +
                                "	used \n" +
                                " * CSVExportMatches automatically switches the delimiter to simicolon if \n" +
                                "	\",\" is found to be the decimal separator in the current local\n" +
                                " * saving the result-file as .txt, .txt.gz, .tsv, or .tsv.gz automatically \n" +
                                "	switches to a tab-separated output.\n" +
                                " * if for some reason CSVExportMatches is set to write out CSV with \",\" \n" +
                                "	as delimiter under a german local then all floating-point values get \n" +
                                "	surounded by quotes.\n" +
                                " * new \"peaklist reader\" that forwards a single spectrum from memory.\n" +
                                "	mainly used in the annotator project\n" +
                                " * new supscores:\n" +
                                "	CCPepFragmentCount : how many unique cross-linker stub modified \n" +
                                "		peptideIon fragments where found\n" +
                                "	CCPepFragmentError: the smallest error for cross-linker stub modified \n" +
                                "		peptideIon fragments  \n" +
                                " * BugFix in crosslinkerrestrictedLoss\n" +
                                " * new setting \"TransferLossToBase:true|false\"\n" +
                                "	if set to true and isotope cluster is matched to a loss xi will try \n" +
                                "	to find a isotope-cluster for the basic (non-lossy) fragment even\n" +
                                "	if the isotope cluster is contained in another isotope cluster\n" +
                                " * BugFix in Spectra.cloneTopPeaksRolling to ensure the right number of \n" +
                                "	peaks are considered \n" +
                                " * the origin of a spectrum can be overwriten (used in the rebase filter)\n" +
                                " * Error is now calculated for both MS1 and MS2 as (observed - calculated)/calculated*1000000\n" +
                                " * Digestion defintions can have now a minimum peptide length defined\n" +
                                " * modified the retention-time tool to transfer a lot of other metadata.\n" +
                                " * bugfix for asymteric (heterobifuctional) cross-linker assigning wrong \n" +
                                "    penalities for linkable aminoacids\n" +
                                " * BugFix for empty zip-files crashing xi\n" +
                                " * new  Filter that tries to provide new candidate m/z values and charge \n" +
                                "    states based on precursor peaks found in the MS2 spectra\n" +
                                " * NormalizerML can now be instructed to replace missing subscore with a \n" +
                                "    default score via:normalizerml_defaultsubscorevalue:score\n" +
                                "    The score would be the normalized version of the score, so the most \n" +
                                "    sensible replacement would be 1.    \n" +
                                " * GenericTextPopUpMenu now also implements a primitive search function for \n" +
                                "	JTextAreas. It also installs a text-handler that triggers this with \n" +
                                "	ctrl+F. Mainly usfull for finding something in the config\n" +
                                " * Context-menu has now two separators (looks nicer)\n" +
                                " * XiSearch is now by default using the english number format on output\n" +
                                " * NumberFormat for the xiRersult can be switched to a different one\n" +
                                  "v1.6.741\n" +
                                  "     BugFix for LowResolution mode being the always switched on\n" +
                                  "     BugFix for definition for X-ions\n" +
                                  "     Bugfix for recognising settings related to variable modifications per peptide\n" +
                                  "     Default xi-flavour is now SimpleXiProcessMultipleCandidates\n" +
                                  "     Fasta files are now IDed to enable a more correct mzIdentML export\n" +
                                  "     StatusWrites to the database are enabled sooner to be able to report more errors to the user\n" +
                                  "     Spectra can define candidate masses for peptides \n" +
                                  "     CSV-Output reports if peptide matched to a predefined mass for the spectrum\n" +
                                  "     CSV-Output reports if peptides are found as with cross-linker stubs\n" +
                                  "     CSV-Output also report the average non-absolute MS2 error\n" +
                                  "     experimental mz and charge are now also keeped independent of spectrum as these masses can be changed as a result of missing monoisotopic peak detection\n" +
                                  "     Index of the scan in the original file is reported\n" +
                                  "     MGF-files can have a new entry XLPEPMASSES to provide a list of masses as candidates for the peptides that make up the spectrum\n" +
                                  "     moved some settings from  genericly stored values to proper fields in the config\n" +
                                  "     InputFilter can be applied before a search-thread gets to see a spectrum\n" +
                                  "     if xi spends more then 80% percent of its time in GC it will be stopping individual search threads as each thread will require memory and freeing the memory for one make the rest work without constantly running into a gc\n" +
                                  "         - this is part of the watchdog running and will be tested ones every 10 minutes\n" +
                                  "     some different approaches to sorting the results for a spectrum are implemented\n" +
                                  "         - by exceeding vs non-exceeding certain average MS2 error limits\n" +
                                  "         - prioretising peptides to predefined peptide masses\n" +
                                  "     in standard xi make sure progress report happens (if there was a progress) at least ones every 10 seconds per thread\n" +
                                  "     BugFix for Spectra.getTopPeaks\n" +
                                  "     linksitescore is written to the database\n" +
                                  "     write the original (unfiltered unmodified) spectrum to the database\n" +
                                  "v1.6.739\n"+
                                  "     Bugfix in watchog  \n" +
                                  "     shifted the database ping from the watchdo to the status interface to prevent concurrent updates on the same databse row \n" +
                                  "     undid a workaround to the database problems (sending interrupts) as these are hopefully not needed anymore \n" +
                                  "v1.6.738\n"+
                                  "     some workarounds some postgresql problems of not coming back from a query. \n" +
                                  "        Unluckely there still cases where I don't have a workaround  \n" +
                                  "v1.6.737\n"+
                                  "     bugfix in connectionpool for not creating new connections \n" +
                                  "v1.6.736\n"+
                                  "     collecting custom config files in a separate list - mainly for forwarding them to the spectrumviewer \n" +
                                  "     XiVersion now has two commandline arguments -- gui to show a window with the version information and -v to just print out the version \n" +
                                  "v1.6.735\n"+
                                  "     Annotated peaklist will now automatically compressed if the file ends in .gz \n" +
                                  "     Simple Xi Gui now appends the xi version to the selected file name \n" +
                                  "     Check for non-stoped thread at the end ignores daemon tasks \n" +
                                  "     SimpleXiGui starts search in own ThreadGroup - interface gets not killed at the end \n" +
                                  "     status of restarted searches should be more informative \n" +
                                  "v1.6.734\n"+
                                  "     config containing spaces around numeric values caused crashes \n" +
                                  "     search pings the database regularly to show that it is still running \n" +
                                  "     Search should stop if the search gets flaged as delted in the database \n" +
                                  "     further improvments for handling of databse disconnects \n " +
                                  "v1.6.733\n"+
                                  "     BugFix fragments matched to  missing monoisotopic peaks where using a slightly wrong mass \n" +
                                  "     fixed a java 9 related problem of reading the default config from within the jar-file \n" +
                                  "     Watchdog that kills xi if no progress hs happened in a while(30 minutes) \n" +
                                  "     BugFix related to cleanup after xi trying to daemonise threads - that does not work \n" +
                                  "     Options MAX_MODIFIED_PEPTIDES_PER_PEPTIDE and MAX_MODIFICATION_PER_PEPTIDE got promoted to proper config options\n" +
                                  "     Two new optional config options MAX_MODIFIED_PEPTIDES_PER_PEPTIDE_FASTA, MAX_MODIFICATION_PER_PEPTIDE_FASTA. if not set explicitly then the non-fasta values are used.\n" +
                                  "     the new options are now aplied when digesting FASTA-files that contain expected variable modifications \n" +
                                  "     Improved handling of databse disconnects \n " +
                                  "v1.6.732\n"+
                                  "     removed unused \"aminoacids\" \n" +
                                  "     bugfix for reading DBConfig if it was on network-path \n" +
                                  "     average MSError subscores for each peptide \n" +
                                  "     BugFix elution time not forwared correctly \n" +
                                  "     Spectra write the original unprocessed spectra out\n" +
                                  "     BugFix for randomized dataabse \n" +
                                  "     Spectra can now be cloned with top peaks in a roling window \n" +
                                  "     Denoise filter on spectra prior matching \n" +
                                  "     BugFix for spectra with unknown charge state in conectionn with additional defined charge states \n" +
                                  "     Maximum average MS2 error can be enforced for results writen out \n" +
                                  "     XiSearch should no longer keep running even so nonrecoverable errors have occured \n" +
                                  "v1.6.731\n"+
                                  "     BugFix: Cleavable Cross-linekr stubs having the wrong fragment mass\n" +
                                  "v1.6.730\n"+
                                  "     BugWorkaround: Not using FUArithmeticScored ocurence as it seem to have a bug\n" +
                                  "v1.6.729\n" +
                                  "     Feature: Use FUArithmeticScoredOccurence hwne the fastutil fragmentree is used\n" +
                                  "     Bugfix for permuted peptides\n" +
                                  "     New setting Tolerance:candidate: for setting using a different fragmenttolerance during candidate selection\n" +
                                  "     Per default priority is now given to linear modified peptides" +
                                  "v1.6.728\n"+
                                  "     First peptide is always the peptide with more fragment matches\n" +
                                  "     Support for cross-linker stub containing fragments\n" +
                                  "     cross-linker containing fragments can become part of the fragmenttree\n" +
                                  "     Soem rework on the thread cleanup post search\n" +
                                  "     Permutated peptides are assigned to the same decoy protein as the originating peptide belonged to\n" +
                                  "     Removed some java finalaze methods as these seem to have lead to memory leaks\n" +
                                  "     Linear Peptides can now prioretizeed if they have the same number of fragments matched and optionally if they have the same sequence as both peptides combined of teh top-scoring match\n" +
                                  "v1.6.724\n"+
                                  "     Alpha candidate selection is pulling in more modification states for peptides\n" +
                                  "     BugFix - peptides for a match get now ordered by number of fragments matching to them\n" +
                                  "     Using a the FastUtil library to reduce memory consumption\n" +
                                  "     Some setting handled by RunConfig.StoreObject and restoreObject are now proper config settings (more need to be cleaned here\n" +
                                  "     remove Annotations from spectra after scoring and form all non-topranknign ones also the peaks\n" +
                                  "     Spectra can now also searched with additional m/yz offsets to account for miss-assignements of mono-isotopic peaks in the massspec\n" +
                                  "     FragmentLookup can now do the candidate selection on it's own. First step to different candidate selection methods" +
                                  "v1.6.723\n"+
                                  "     Alpha candidate selection is pulling in more modification states for peptides\n" +
                                  "v1.6.722\n"+
                                  "     BugFix for custom run and scan number information\n" +
                                  "     Memory optimizations\n" +
                                  "v1.6.721\n"+
                                  "     BugFix for custom run and scan number information\n" +
                                  "v1.6.720\n"+
                                  "     support for defining additional charge states and m/z values in mgf files\n" +
                                  "v1.6.719\n"+
                                  "     First try of mzML support\n" +
                                  "     ResultWriters throw IOExceptions"+ 
                                  "v1.6.718\n"+
                                  "     BugFix for multiple acquisitions searched\n"+ 
                                  "v1.6.717\n"+
                                  "     new option MAXPEAKCANDIDATES to restrict the maximum ambiguity a peak can have to be considered for candidate selection\n"+ 
                                  "v1.6.716\n"+
                                  "     new integer based fragment tree\n"+ 
                                  "v1.6.714\n"+
                                  "     Speed up for candidate selection\n"+ 
                                  "v1.6.713\n"+
                                  "     BugFix: manual start of database searches failed\n"+ 
                                  "v1.6.712\n"+
                                  "     BugFix: comma \",\" in raw-file names\n"+ 
                                  "     BugFix: Multisptep digestion\n" + 
                                  "     BugFix: Missing remove() in PermArray\n" +
                                  "v1.6.711\n"+
                                  "     WorkAround - some buffer threads appear not to close down - this is now just a workaround" + 
                                  "v1.6.693\n" + 
                                  "     BugFix: optional setting \"ConservativeLosses\" was ignored\n" +
                                  "     BugFix: LinearCrosslinker now does not declare peptides as linkable"+
                                  "v1.6.688\n" + 
                                  "     Command-line support for xi\n"+
                                  "v1.6.683\n" + 
                                  "     bugfix for writing out proteins to the xi3 database that have no name but only accession numbers \n"+
                                  "v1.6.681\n" + 
                                  "     bugfix missing new line when writing out spectrum_sources \n"+
                                  "v1.6.679\n" + 
                                  "     Still bugfix for duplicat ids writen to database \n"+
                                  "v1.6.676\n" + 
                                  "     bugfix for duplicat ids writen to database again\n"+
                                  "v1.6.675\n" + 
                                  "     bugfix for duplicat ids writen to database\n"+
                                  "v1.6.671\n" + 
                                  "     xi3 database output\n"+
                                  "     csv-output - updted preceding and succeding amino-acids\n"+
                                  "     possibility to define arbitrary regular expressions for parsing mgf-files\n"+
                                  "     disabling the debug-window now also does not load it anymore (no graphical system needed anymore)\n"+
                                  "     update for restarting searches\n"+
                                  "v1.6.658\n" + 
                                  "     BugFix for duplicated keys trying to be writen into database (has_protein-table)\n"+
                                  "v1.6.656\n" + 
                                  "     Bugfix for multistepdigest\n"+
                                  "v1.6.655\n" + 
                                  "     support for stacking of multistep digestion and parallel digestion\n"+
                                  "v1.6.653\n" + 
                                  "     support for multistep digestion and parallel digestion\n"+
                                  "v1.6.650\n" + 
                                  "     bugfix for protein haveing null name not writen out to database\n"+
                                  "v1.6.649\n" + 
                                  "     limited suport for another openms converted mgf files\n"+
                                  "v1.6.646\n" + 
                                  "     BugFix for annotation of overlapping isotope clusters (extension of isotope cluster to lighter peaks)\n"+
                                  "     BugFix for generation of reversed sequences could lead to protein names of just \"REV_\"\n"+
                                  "     BugFix Version.toString() failing when extension is null\"\n"+
                                  "     Updated default config for offline Xi\n" +
                                  "v1.6.643\n" + 
                                  "     BugFix for MSM-iterator expecting FinneganScanNumber to be lower case\n"+
                                  "     Some changes to cross-linker needed for mzIdentML export from xiFDR\n"+
                                  "v1.6.642\n" + 
                                  "     small bugfix in no search related functions\n"+
                                  "v1.6.641\n" + 
                                  "     Reduced Memory consumtion\n" +
                                  "     OpenMS-Style mgf files can now be read in\n"+
                                  "v1.5.636\n" + 
                                  "     bugfix ZipMSMListIteraotor now opens the zip file as read only\n" +
                                  "     Decoys can now be forced to be considered even if they are also found as targets\n"+
                                  "v1.5.632\n" + 
                                  "     bugfix DBRunConfig\n" +
                                  "v1.5.628\n" + 
                                  "     bugfix DBRunConfig\n" +
                                  "v1.5.627\n" + 
                                  "     SLight bugfix for memory-display in debug-window\n"+
                                  "v1.5.626\n" + 
                                  "     Possibility for automatic gc when memory falls below 10mb - mainly for debug-purposes to log how much memory is actually use/available \n" +
                                  "     detection of possible linears now uses unique peak-matches instead of primary explanations - to make it more robust in not flagging likely linears as autovalidated\n"+
                                  "v1.5.624\n" + 
                                  "     BugFixes for the debug-window not showning memory informations" +
                                  "     enabled modification to be c- or n-terminal\n"+
                                  "v1.5.619\n" + 
                                  "     BugFixes in aminoacid iterator for peptides and sequences" +
                                  "     ToleranceUnit now provides functions to calculate the error as defined by the tolerance unit - meaning as Da-unit or ppm unit\n"+
                                  "v1.5.618\n" + 
                                  "     Several BugFixes and some adaptions needed for the on demand annotator" +
                                  "v1.5.609\n" +
                                  "     Bugfix+: hopefully a \"bugfix\" for searches not finish writing - actually more of a workaround \n" +
                                  "v1.5.594\n" +
                                  "     Bugfix+: for randomly generated peptides \n" +
                                  "v1.5.592\n" +
                                  "     Bugfix: Randomly generated decoy peptides where not correctly flaged up as beeing the same N- and C- terminal wise as the original peptide\n" +
                                  "v1.5.591\n" +
                                  "     Bugfix: Likely linear matches where auto-validated\n" +
                                  "v1.5.588\n" +
                                  "     Bugfix: unrestricted crosslinker was screwed by the narry cross-linker\n" +
                                  "v1.5.587\n" +
                                  "     Bugfix: sometimes a match of two peptides that are not really cross-linkable made it through and then crashed in the new LinkSiteDelta score.\n" +
                                  "         now peptides get tested for linkability before doing a full matching.\n" +
                                  "v1.5.584\n" +
                                  "     Bugfix: sometimes a match of two peptides that are not really cross-linkable made it through and then crashed in the new LinkSiteDelta score.\n" +
                                  "         now peptides get tested for linkability before doing a full matching.\n" +
                                  "v1.4.582 \n" +
                                  "     minimum requirementsfilter is back\n"+
                                  "     Link-Site weight ignores losses of more then 3 things\n"+
                                  "     Link-Site weight losses have less impact\n"+
                                  "     xi-version used during search is written into search notes\n"+
                                  "v1.4.581 \n" +
                                  "     new xi version with updated scores and autovalidation" +
                                  "v1.3.560 \n" +
                                  "     Adam HSA research \n"+
                                  "v1.3.553 \n" +
                                  "     Version for Marta Mendes ETD corrected\n" +
                                  "v1.3.600\n" +
                                  "     Added flushing of result writers.\n"+
                                  "     Multithreaded gather data for chacking maximum peptide-sizes\n" + 
                                  "     restructured filter into sub-folders\n"+ 
                                  "v1.3.400\n" +
                                  "     Deadlock detectionz\n" +
                                  "     empty files in zips are ignored\n" +
                                  "     Targeted modifciations can be enabled by dummy crosslinker with name TargetModification\n" +
                                  "     Compatible to mgf files of newer versions of ProteoWizard MSConvert\n"+ 
                                  "     Support for low-resolution ms2\n" +
                                  "     OpenModification output can be restricted to only modified peptides" +
                                  "v1.3.355\n" +
                                  "     AutoValidation only validates with score >7 and possible linears must have some support to show, that they are cross-linked\n" +
                                  "     Better process-information\n" +
                                  "     Database-output changed to ad some new fields (e.g. Peptide-length)\n"+
                                  "     Digestion: AminoAcids defined for specificity and restriction that are not known will be ignored\n" +
                                  "     Support for ms-convert generated mgf-files\n" +
                                  "     Support for MassMatrix-generated mgf-files\n" +
                                  "     Fasta-files can be gziped\n"+
                                  "     MGF-Files can be gziped or zip-compressed\n" +
                                  "     Fasta-header are parsed and split into name accession and description\n" +
                                  "     Some changes, to make the non-guiparts compatible with jdk 1.5\n"+
                                  "     ScanFilter tries to ignore the extension\n" +
                                  "     BugFix for spectra, that are empty or would create an empty mgc-spectra\n" +
                                  "     BugFix for ProteinDiscoverer generated mgf-files\n" +
                                  "     BugFix in AsymetricAminoAcidRestricted Cross-linker to actually be Asymetric\n" +
                                  "     BugFix - Format of the CSV-export\n" +
                                  "     BugFix ToleranceUnit to handle \"0 da\"\n" +
                                  "     BugFix for some APL-files\n"+
                                  "     BugFix to the msm-iterator\n" +
                                  "     BugFix for weighted cross-linker sites\n" +
                                  "     BugFix in some places where a maximum charge state of 10 defined - removed\n" +
                                  "     BugFix to the isotop-detection\n" +
                                  "v1.3.316\n" +
                                  "     BugFix release\n" +
                                  "     second-last aminoacid being crosslinkable not beeing regocnised\n" +
                                  "     exported precursor error was wrong\n" +
                                  "     APL-input had an error in the first run of gathering information about the APL-file\n"+
                                  "     additionally Lysin got some hardcoded precedence for the crosslinking-site"+
                                  "1.2.258\n" +
                                  "     Label working now - but schemes are ignored - so only Heavy and light no medium\n" +
                                  "     Bugfix for the cutoff heuristics" +
                                  "1.2.259\n" +
                                  "     BugFix for digestion and crosslinker did not handle labeled amino-acids\n" + 
                                  "1.2.315\n" + 
                                  "     BugFix for second-last aminoacid being crosslinkable not beeing regocnised\n" +
                                  "     Hardcoded preference for lysins (until the weighted implementaions is up and running) \n"+
                                  "     A lot of code-cleanup" ;


    public static String getVersionString() {
        return version.toString();
    }


    public static void main(String[] args) {
        if (args.length >0) {
            if (args[0].toLowerCase().trim().contentEquals("--gui")) {
                javax.swing.JOptionPane.showMessageDialog(null, getVersionString() + "\n"+changes, "Version" + getVersionString(), javax.swing.JOptionPane.PLAIN_MESSAGE);
                return;
            }else if (args[0].toLowerCase().trim().contentEquals("-v")) {
                System.out.println(getVersionString());
                return;
            }
            
            
        } 
        
        System.err.println("Current Version: "+ getVersionString());
        System.err.println(changes);
        System.err.println("Current Version: "+ getVersionString());
 
    }


}
