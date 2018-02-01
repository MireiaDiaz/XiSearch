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
package rappsilber.config;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rappsilber.applications.SimpleXiProcessLinearIncluded;
import rappsilber.applications.XiProcess;
import rappsilber.ms.ToleranceUnit;
import rappsilber.ms.crosslinker.CrossLinker;
import rappsilber.ms.dataAccess.AbstractSpectraAccess;
import rappsilber.ms.dataAccess.StackedSpectraAccess;
import rappsilber.ms.dataAccess.output.ResultWriter;
import rappsilber.ms.score.ScoreSpectraMatch;
import rappsilber.ms.sequence.AminoAcid;
import rappsilber.ms.sequence.AminoLabel;
import rappsilber.ms.sequence.AminoModification;
import rappsilber.ms.sequence.NonAminoAcidModification;
import rappsilber.ms.sequence.SequenceList;
import rappsilber.ms.sequence.digest.Digestion;
import rappsilber.ms.sequence.ions.BasicCrossLinkedFragmentProducer;
import rappsilber.ms.sequence.ions.CrosslinkedFragment;
import rappsilber.ms.sequence.ions.DoubleFragmentation;
import rappsilber.ms.sequence.ions.Fragment;
import rappsilber.ms.sequence.ions.loss.Loss;
import rappsilber.ms.spectra.annotation.Averagin;
import rappsilber.ms.spectra.annotation.IsotopPattern;
import rappsilber.ui.LoggingStatus;
import rappsilber.ui.StatusInterface;
import rappsilber.utils.SortedLinkedList;
import rappsilber.ms.sequence.ions.CrossLinkedFragmentProducer;
import rappsilber.utils.Util;

/**
 *
 * @author Lutz Fischer <l.fischer@ed.ac.uk>
 */
public abstract class AbstractRunConfig implements RunConfig {

    public static final int DEFAULT_MAX_LOSSES  =  4;
    public static final int DEFAULT_MAX_TOTAL_LOSSES = 6;
    
    public static AbstractRunConfig DUMMYCONFIG = new AbstractRunConfig() {};

    private ArrayList<Method> m_losses = new ArrayList<Method>();

    private ArrayList<AminoModification> m_fixed_mods = new ArrayList<AminoModification>();
    private ArrayList<AminoModification> m_var_mods = new ArrayList<AminoModification>();
    private ArrayList<AminoModification> m_known_mods = new ArrayList<AminoModification>();
    private ArrayList<NonAminoAcidModification> m_NTermPepMods = new ArrayList<NonAminoAcidModification>();
    private ArrayList<NonAminoAcidModification> m_CTermPepMods = new ArrayList<NonAminoAcidModification>();
    private ArrayList<NonAminoAcidModification> m_NTermProtMods = new ArrayList<NonAminoAcidModification>();
    private ArrayList<NonAminoAcidModification> m_CTermProtMods = new ArrayList<NonAminoAcidModification>();
    private ArrayList<AminoLabel> m_label = new ArrayList<AminoLabel>();
    private HashMap<Integer,ArrayList<AminoLabel>> m_labelShemes = new HashMap<Integer,ArrayList<AminoLabel>>();

    private HashMap<AminoAcid,ArrayList<AminoModification>> m_mapped_fixed_mods = new HashMap<AminoAcid, ArrayList<AminoModification>>();
    private HashMap<AminoAcid,ArrayList<AminoModification>> m_mapped_var_mods  = new HashMap<AminoAcid, ArrayList<AminoModification>>();
    private HashMap<AminoAcid,ArrayList<AminoModification>> m_mapped_known_mods  = new HashMap<AminoAcid, ArrayList<AminoModification>>();
    private HashMap<AminoAcid,ArrayList<AminoLabel>> m_mapped_label = new HashMap<AminoAcid, ArrayList<AminoLabel>>();
    private ArrayList<CrossLinker> m_crosslinker = new ArrayList<CrossLinker>();
    private Digestion   m_digestion;
    private ToleranceUnit m_PrecoursorTolerance;
    private ToleranceUnit m_FragmentTolerance;
    private ToleranceUnit m_FragmentToleranceCandidate;
    private IsotopPattern m_isotopAnnotation = new Averagin();
    private int           m_topMGCHits = 10;
    private int           m_topMGXHits = -1;
    private StatusInterface m_status = new status_multiplexer();
    private ArrayList<StatusInterface> m_status_publishers = new ArrayList<StatusInterface>(2);
    private int           m_maxpeps = 2;
    
    private int           m_maxFragmentCandidates = -1;
    
    /**
     * a flag to indicate if the current search should be stopped.
     * I.e. all search threads should close down after this flag is set.
     */
    private volatile boolean       m_searchStoped=false;
    
    private SequenceList.DECOY_GENERATION m_decoyTreatment = SequenceList.DECOY_GENERATION.ISTARGET;
    
    

    /** This flags up, if we do a  low-resolution search, meaning de-isotoping is ignored */
    private Boolean       m_LowResolution = null;
    
    /**
     * the maximum mass of a peptide to be considered.
     * for values &lt;0 this gets defined by the largest precursor in the peak-list
     */
    private double        m_maxPeptideMass = -1;

    /**
     * check for peptides being non-covalently bound
     */
    private boolean       m_CheckNonCovalent= false;

    /**
     * Also match linear peptides to spectra
     */
    private boolean       m_EvaluateLinears = true;
    
    /**
     * the number of concurent search threads
     * values &lt;0 should indicate that a according number of detected cpu-cores 
     * should not be used.
     * E.g. if the computer has 16 cores and the setting is -1 then 15 search 
     * threads should be started
     * otherwise the number of threads defined should be used.
     */
    private int           m_SearchThreads = calculateSearchThreads(-1);
    
    /**
     * Should only the top-ranking matches be written out?
     */
    private boolean       m_topMatchesOnly = false;
    
    /**
     * list of objects that can produce cross-linked fragments based on linear fragments
     */
    private ArrayList<CrossLinkedFragmentProducer>  m_crossLinkedFragmentProducer = new ArrayList<>();   
    
    /**
     * each spectrum can be searched with a list of additional m/z offsets to 
     * the precursor 
     */
    ArrayList<Double> m_additionalPrecursorMZOffsets = null;

    /**
     * spectra with unknown precursor charge state can be searched with a list of 
     * additional m/z offsets to the precursor 
     */
    ArrayList<Double> m_additionalPrecursorMZOffsetsUnknowChargeStates;

    /**
     * list of objects that can produce cross-linked fragments based on 
     * linear fragments
     * that should also be considered for candidate selection
     */
    private ArrayList<CrossLinkedFragmentProducer>  m_primaryCrossLinkedFragmentProducer = new ArrayList<>();   
    
    private int m_maxModificationPerPeptide = 3;

    private int m_maxModifiedPeptidesPerPeptide = 20;
    
    private boolean m_maxModificationPerFASTAPeptideSet  =false;
    private int m_maxModificationPerFASTAPeptide = m_maxModificationPerPeptide;

    private boolean m_maxModifiedPeptidesPerFASTAPeptideSet  =false;
    private int m_maxModifiedPeptidesPerFASTAPeptide = m_maxModifiedPeptidesPerPeptide;
    
    {
        addStatusInterface(new LoggingStatus());
        m_crossLinkedFragmentProducer.add(new BasicCrossLinkedFragmentProducer());
    }


    SortedLinkedList<ScoreSpectraMatch> m_scores = new SortedLinkedList<ScoreSpectraMatch>();

    private HashMap<Object, Object> m_storedObjects = new HashMap<Object, Object>();


    private class status_multiplexer implements StatusInterface {
        
        String status;
            
        public void setStatus(String status) {
            this.status = status;
            for (StatusInterface si : m_status_publishers) {
                si.setStatus(status);
            }
        }

        public String getStatus() {
            return status;
        }
        
    }




    HashMap<String,AminoAcid> m_AminoAcids = new HashMap<String, AminoAcid>();
    {
        for (AminoAcid aa : AminoAcid.getRegisteredAminoAcids()) {
            m_AminoAcids.put(aa.SequenceID,aa);
        }

        DoubleFragmentation.setEnable(false);
    }


    private int m_NumberMGCPeaks = -1;

    private int m_max_missed_cleavages = 4;

    private ArrayList<Method> m_fragmentMethods = new ArrayList<Method>();
    private ArrayList<Method> m_secondaryFragmentMethods = new ArrayList<Method> ();
    private ArrayList<Method> m_lossMethods = new ArrayList<Method>();


    private ArrayList<String> m_checkedConfigLines = new ArrayList<String>();

//    private ArrayList<Method> m_fragments = new ArrayList<Method>();


//    public ArrayList<Fragment> fragment(Peptide p) {
//        ArrayList<Fragment> returnList = new ArrayList<Fragment>();
//        // call each registered fragmentation function
//        for (Method m :m_fragments) {
//            Object ret = null;
//            try {
//
//                ret = m.invoke(null, p);
//
//            } catch (IllegalAccessException ex) { //<editor-fold desc="and some other" defaultstate="collapsed">
//                Logger.getLogger(Fragment.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IllegalArgumentException ex) {
//                Logger.getLogger(Fragment.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (InvocationTargetException ex) {
//                Logger.getLogger(Fragment.class.getName()).log(Level.SEVERE, null, ex);//</editor-fold>
//            }
//            if (ret != null && ret instanceof ArrayList) {
//                returnList.addAll((ArrayList<Fragment>)ret);
//            }
//        }
//        return returnList;
//    }
//
//    public void registerFragmentClass(Class<? extends Fragment> c) throws NoSuchMethodException {
//            Method m = c.getMethod("fragment", Peptide.class);
//            if (!m_fragments.contains(m))
//                m_fragments.add(m);
//    }


    public void addFixedModification(AminoModification am) {
        AminoAcid base = am.BaseAminoAcid;
        m_fixed_mods.add(am);
        // sync with label
        ArrayList<AminoLabel> ml = m_mapped_label.get(base);
        if (ml != null)
            for (AminoLabel al : ml)
                m_fixed_mods.add(new AminoModification(am.SequenceID + al.labelID, base, am.mass + al.weightDiff));
       m_mapped_fixed_mods = generateMappings(m_fixed_mods);
       addAminoAcid(am);
    }

    public void addVariableModification(AminoModification am) {
        AminoAcid base = am.BaseAminoAcid;
        m_var_mods.add(am);
        // sync with label
        ArrayList<AminoLabel> ml = m_mapped_label.get(base);
        if (ml != null)
            for (AminoLabel al : ml)
                m_var_mods.add(new AminoModification(am.SequenceID + al.labelID, base, am.mass + al.weightDiff));
        m_mapped_var_mods = generateMappings(m_var_mods);
        addAminoAcid(am);
    }

    public void addLabel(AminoLabel al) {
        AminoAcid base = al.BaseAminoAcid;
        m_label.add(al);
        ArrayList<AminoModification> dependend = getVariableModifications(al.BaseAminoAcid);
        m_mapped_label = generateLabelMappings(m_label);

        // duplicate modifications into labeled modifications
        ArrayList<AminoModification> cm = getVariableModifications(al.BaseAminoAcid);
        if (cm != null)
        for (AminoModification am : (ArrayList<AminoModification>)cm.clone())
            m_var_mods.add(new AminoModification(am.SequenceID + al.labelID, base, am.mass + al.weightDiff));
        cm = getFixedModifications(al.BaseAminoAcid);
        if (cm != null)
        for (AminoModification am : (ArrayList<AminoModification>)cm.clone())
            m_fixed_mods.add(new AminoModification(am.SequenceID + al.labelID, base, am.mass + al.weightDiff));

        // update mappings
        m_mapped_fixed_mods = generateMappings(m_fixed_mods);
        m_mapped_var_mods = generateMappings(m_var_mods);
        m_AminoAcids.put(al.SequenceID,al);
    }



    protected HashMap<AminoAcid,ArrayList<AminoModification>> generateMappings(ArrayList<AminoModification> modifications){
        HashMap<AminoAcid,ArrayList<AminoModification>> map = new HashMap<AminoAcid, ArrayList<AminoModification>>();
        for (AminoModification am : modifications) {
            ArrayList<AminoModification> list = map.get(am.BaseAminoAcid);
            if (list == null) {
                list = new ArrayList<AminoModification>();
                map.put(am.BaseAminoAcid, list);
            }
            list.add(am);
        }
        return map;
    }

    protected HashMap<AminoAcid,ArrayList<AminoLabel>> generateLabelMappings(ArrayList<AminoLabel> label){
        HashMap<AminoAcid,ArrayList<AminoLabel>> map = new HashMap<AminoAcid, ArrayList<AminoLabel>>();
        for (AminoLabel am : label) {
            ArrayList<AminoLabel> list = map.get(am.BaseAminoAcid);
            if (list == null) {
                list = new ArrayList<AminoLabel>();
                map.put(am.BaseAminoAcid, list);
            }
            list.add(am);
        }
        return map;
    }


    public Digestion getDigestion_method() {
        return m_digestion;
    }


    public ArrayList<AminoLabel> getLabel() {
            return m_label;
    }

    public ArrayList<AminoLabel> getLabel(AminoAcid aa) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ArrayList<AminoModification> getFixedModifications() {
        return m_fixed_mods;
    }

    public ArrayList<AminoModification> getFixedModifications(AminoAcid aa) {
        return m_mapped_fixed_mods.get(aa);
    }

    public ArrayList<AminoModification> getVariableModifications() {
        return m_var_mods;
    }

    public ArrayList<AminoModification> getKnownModifications() {
        return m_known_mods;
    }
    public ArrayList<AminoModification> getKnownModifications(AminoAcid aa) {
        return m_mapped_known_mods.get(aa);
    }
    
    
    public ArrayList<NonAminoAcidModification> getVariableNterminalPeptideModifications() {
        return m_NTermPepMods;
    }

    public ArrayList<NonAminoAcidModification> getVariableCterminalPeptideModifications() {
        return m_CTermPepMods;
    }

    public void addVariableNterminalPeptideModifications(NonAminoAcidModification mod) {
        m_NTermPepMods.add(mod);
    }

    public void addVariableCterminalPeptideModifications(NonAminoAcidModification mod) {
        m_CTermPepMods.add(mod);
    }


    public ArrayList<AminoModification> getVariableModifications(AminoAcid aa) {
        return m_mapped_var_mods.get(aa);
    }

    public ArrayList<CrossLinker> getCrossLinker() {
        return m_crosslinker;
    }

    public ToleranceUnit getPrecousorTolerance() {
        return m_PrecoursorTolerance;
    }

    public ToleranceUnit getFragmentTolerance() {
        return m_FragmentTolerance;
    }
    
    /**
     * defaults to getFragmentTolerance
     * @return the m_FragmentToleranceCandidate
     */
    public ToleranceUnit getFragmentToleranceCandidate() {
        return m_FragmentToleranceCandidate == null? m_FragmentTolerance : m_FragmentToleranceCandidate;
    }

    /**
     * @param m_FragmentToleranceCandidate the m_FragmentToleranceCandidate to set
     */
    public void setFragmentToleranceCandidate(ToleranceUnit m_FragmentToleranceCandidate) {
        this.m_FragmentToleranceCandidate = m_FragmentToleranceCandidate;
        //if fragment tolerance is to big - switch to low-resolution mode
        if (getFragmentToleranceCandidate().getUnit().contentEquals("da") && getFragmentToleranceCandidate().getValue() > 0.06)
            m_LowResolution = true;
        else {
            m_LowResolution = false;
        }
    }

    public int getNumberMgcPeaks() {
        return m_NumberMGCPeaks;
    }

    public int getMaxMissCleavages() {
        return m_max_missed_cleavages;
    }

    public ArrayList<Fragment> includeLosses(ArrayList<Fragment> fragments,  boolean comulative) {

        ArrayList<Fragment> returnList = new ArrayList<Fragment>();
        //returnList.addAll(AminoAcidRestrictedLoss.createLossyFragments(fragments));
        //returnList.addAll(AIonLoss.createLossyFragments(Fragments));
        //returnList.addAll( );
        // H20
        if (comulative) {
            for (Method m :m_losses) {
                Object ret = null;
                try {

                    m.invoke(null, fragments, m_crosslinker, true);

                } catch (IllegalAccessException ex) { //<editor-fold desc="and some other" defaultstate="collapsed">
                    Logger.getLogger(Fragment.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(Fragment.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(Fragment.class.getName()).log(Level.SEVERE, null, ex);//</editor-fold>
                }
            }
        } else {
            for (Method m :m_losses) {
                Object ret = null;
                try {

                    ret = m.invoke(null, fragments, false);

                } catch (IllegalAccessException ex) { //<editor-fold desc="and some other" defaultstate="collapsed">
                Logger.getLogger(Fragment.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Fragment.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Fragment.class.getName()).log(Level.SEVERE, null, ex);//</editor-fold>
                }
                if (ret != null && ret instanceof ArrayList) {
                    returnList.addAll((ArrayList<Fragment>)ret);
                }
            }
        }
        return returnList;
    }

    public void registerLossClass(Class<? extends Fragment> c) throws NoSuchMethodException {
        //find the createLossyFragments method
        for (Method m : c.getMethods()) {
            if (m.getName().contentEquals("createLossyFragments")) {
                m_losses.add(m);
            }
        }
       // m_losses.add(c.getMethod("createLossyFragments", new Class[]{ArrayList.class, boolean.class}));

    }

    public void addScore(ScoreSpectraMatch score) {
        m_scores.add(score);
    }

    public SortedLinkedList<ScoreSpectraMatch> getScores() {
        return m_scores;
    }

    
    public int getMaxCrosslinkedPeptides() {
        return m_maxpeps;
    }

    public void setMaxCrosslinkedPeptides(int maxpeps) {
        this.m_maxpeps=maxpeps;
    }
    
    public IsotopPattern getIsotopAnnotation() {
        return m_isotopAnnotation;
    }


    public AminoAcid getAminoAcid(String SequenceId) {
        return m_AminoAcids.get(SequenceId);
    }
    
    public Collection<AminoAcid> getAllAminoAcids() {
        return m_AminoAcids.values();
    }

    public void addAminoAcid(AminoAcid aa) {
        m_AminoAcids.put(aa.SequenceID,aa);
    }

    public void addKnownModification(AminoModification am) {
        AminoAcid base = am.BaseAminoAcid;
        m_known_mods.add(am);
        // sync with label
        ArrayList<AminoLabel> ml = m_mapped_label.get(base);
        if (ml != null)
            for (AminoLabel al : ml)
                m_known_mods.add(new AminoModification(am.SequenceID + al.labelID, base, am.mass + al.weightDiff));
        m_mapped_known_mods = generateMappings(m_known_mods);
        addAminoAcid(am);
    }
    
    public ArrayList<Method> getFragmentMethods() {
        return m_fragmentMethods;
    }

    public ArrayList<Method> getSecondaryFragmentMethods() {
        return m_secondaryFragmentMethods;
    }

    public ArrayList<Method> getLossMethods() {
        return m_lossMethods;
    }


    public void register(AminoAcid aa) {
        this.m_AminoAcids.put(aa.SequenceID, aa);
    }

    public void setIsotopAnnotation(IsotopPattern i) {
        m_isotopAnnotation = i;
    }

    public void storeObject(Object key, Object value) {
        m_storedObjects.put(key, value);
    }

    public Object retrieveObject(Object key) {
        return m_storedObjects.get(key);
    }


    public boolean retrieveObject(Object key, boolean defaultValue) {
        Object o = retrieveObject(key);
        if (o == null)
            return defaultValue;
        if (o instanceof Boolean)
            return (Boolean) o;

        String s = o.toString();
        return getBoolean(s, defaultValue);
    }

    public String retrieveObject(Object key, String defaultValue) {
        Object o = retrieveObject(key);
        if (o == null)
            return defaultValue;

        return o.toString();
    }

    public double retrieveObject(Object key, double defaultValue) {
        Object o = retrieveObject(key);
        if (o == null)
            return defaultValue;

        if (o instanceof Number)
            return ((Number) o).doubleValue();

        return Double.parseDouble(o.toString().trim());
    }

    public int retrieveObject(Object key, int defaultValue) {
        Object o = retrieveObject(key);
        if (o == null)
            return defaultValue;

        if (o instanceof Number)
            return ((Number) o).intValue();

        return Integer.parseInt(o.toString().trim());
    }

    public long retrieveObject(Object key, long defaultValue) {
        Object o = retrieveObject(key);
        if (o == null)
            return defaultValue;

        if (o instanceof Number)
            return ((Number) o).longValue();

        return Long.parseLong(o.toString().trim());
    }

    /**
     * @param crosslinker the crosslinker to set
     */
    public void setCrosslinker(CrossLinker[] crosslinker) {
        for (CrossLinker cl : crosslinker) {
            addCrossLinker(cl);
        }
    }

    /**
     * adds a crosslinker to the list of searched crosslinkers
     * @param crosslinker
     */
    public void addCrossLinker(CrossLinker crosslinker) {
        m_crosslinker.add(crosslinker);
    }

    /**
     * @param digestion the digestion to set
     */
    public void setDigestion(Digestion digestion) {
        this.m_digestion = digestion;
    }

    /**
     * @param tolerance the tolerance to set
     */
    public void setPrecoursorTolerance(ToleranceUnit tolerance) {
        this.m_PrecoursorTolerance = tolerance;
    }

    /**
     * @param tolerance the tolerance to set
     */
    public void setFragmentTolerance(ToleranceUnit tolerance) {
        this.m_FragmentTolerance = tolerance;
        if (m_LowResolution == null) {
            //if fragment tolerance is to big - switch to low-resolution mode
            if (getFragmentTolerance().getUnit().contentEquals("da") && getFragmentTolerance().getValue() > 0.06)
                setLowResolution(true);
            else {
                setLowResolution(true);
            }
        }
    }

    /**
     * @param mgcPeaks the mgcPeaks to set
     */
    public void setNumberMGCPeaks(int mgcPeaks) {
        this.m_NumberMGCPeaks = mgcPeaks;
    }

    /**
     * @param max the max to set
     */
    public void setMaxMissedCleavages(int max) {
        this.m_max_missed_cleavages = max;
    }

    /**
     * @param max the max to set
     */
    public void setTopMGCHits(int max) {
        this.m_topMGCHits = max;
    }

    /**
     * @param max the max to set
     */
    public int getTopMGCHits() {
        return this.m_topMGCHits;
    }

    public int getTopMGXHits() {
        return this.m_topMGXHits;
    }

    public void setTopMGXHits(int top) {
        this.m_topMGXHits = top;
    }

    public boolean evaluateConfigLine(String line) throws ParseException{
        String[] confLine = line.split(":",2);
        String confName = confLine[0].toLowerCase().trim();
        m_checkedConfigLines.add(line);
        //String className = confLine[1].trim();
        String confArgs = null;
        if (confLine.length >1)
            confArgs = confLine[1].trim();

        if (confName.contentEquals("crosslinker")){
            String[] c = confArgs.split(":",2);
            CrossLinker cl = CrossLinker.getCrossLinker(c[0], c[1], this);
            if (cl.getName().toLowerCase().contentEquals("wipe")) {
                m_crosslinker.clear();
            } else
                addCrossLinker(cl);
                

        } else if (confName.contentEquals("digestion")){
            String[] c = confArgs.split(":",2);
            Digestion d = Digestion.getDigestion(c[0], c[1], this);
            setDigestion(d);

        } else if (confName.contentEquals("modification")) {
            String[] c = confArgs.split(":",3);
            String type = c[0].toLowerCase();
            //confLine = classArgs.split(":",2);
            List<AminoModification> am;

            if (c[1].length() == 0)  {
                am = AminoModification.parseArgs(c[2],this);
            } else {
                am = AminoModification.getModifictaion(c[1], c[2], this);
            }

            if (c[0].toLowerCase().contentEquals("fixed")) {
                for (AminoModification a : am)
                    addFixedModification(a);
            } else if (c[0].toLowerCase().contentEquals("variable")){
                for (AminoModification a : am)
                    addVariableModification(a);
            } else if (c[0].toLowerCase().contentEquals("known")){
                for (AminoModification a : am)
                    addKnownModification(a);
            } else {
                throw new ParseException("Unknown modification Type \"" + c[0] + "\" in " + line,0);
            }
        } else if (confName.contentEquals("cterminalmodification")) {
            String[] c = confArgs.split(":",3);
            String type = c[0].toLowerCase();
            //confLine = classArgs.split(":",2);
            NonAminoAcidModification am = null;

            if (c[1].length() == 0)  {
                am = NonAminoAcidModification.parseArgs(c[2],this).get(0);
            }
//            } else {
//                am = AminoModification.getModifictaion(c[1], c[2], this);
//            }

            if (c[0].toLowerCase().contentEquals("fixed")) {
//                addFixedModification(am);
            } else {
                addVariableCterminalPeptideModifications(am);
            }
        } else if (confName.contentEquals("nterminalmodification")) {
            String[] c = confArgs.split(":",3);
            String type = c[0].toLowerCase();
            //confLine = classArgs.split(":",2);
            NonAminoAcidModification am = null;

            if (c[1].length() == 0)  {
                am = NonAminoAcidModification.parseArgs(c[2],this).get(0);
            }
//            } else {
//                am = AminoModification.getModifictaion(c[1], c[2], this);
//            }

            if (c[0].toLowerCase().contentEquals("fixed")) {
//                addFixedModification(am);
            } else {
                addVariableNterminalPeptideModifications(am);
            }

        } else if (confName.contentEquals("MAX_MODIFIED_PEPTIDES_PER_PEPTIDE")) {
            setMaxModifiedPeptidesPerPeptide(confArgs);
        } else if (confName.contentEquals("MAX_MODIFIED_PEPTIDES_PER_PEPTIDE_FASTA")) {
            setMaxModifiedPeptidesPerFASTAPeptide(confArgs);
        } else if (confName.contentEquals("MAX_MODIFICATION_PER_PEPTIDE")) {
            setMaxModificationPerPeptide(confArgs);
        } else if (confName.contentEquals("MAX_MODIFICATION_PER_PEPTIDE_FASTA")) {
            setMaxModificationPerFASTAPeptide(confArgs);
        } else if (confName.contentEquals("label")) {
            String[] c = confArgs.split(":",3);
            if (c[1].length()==0) {
                addLabel(AminoLabel.parseArgs(c[2], this));
            } else {
                addLabel(AminoLabel.getLabel(c[1], c[2], this));
            }

        } else if (confName.contentEquals("tolerance")) {
            String[] c = confArgs.split(":",2);
            String tType = c[0].toLowerCase();
            if (tType.contentEquals("precursor"))
                setPrecoursorTolerance(ToleranceUnit.parseArgs(c[1]));
            else if (tType.contentEquals("fragment")) {
                setFragmentTolerance(ToleranceUnit.parseArgs(c[1]));

                
            } else if (tType.contentEquals("candidate")) {
                setFragmentToleranceCandidate(ToleranceUnit.parseArgs(c[1]));
            }

        } else if (confName.contentEquals("loss")) {
                Loss.parseArgs(confArgs, this);

        } else if (confName.contentEquals("fragment")) {
            Fragment.parseArgs(confArgs, this);
//        } else if (confName.contentEquals("score")) {
//            String[] c = confArgs.split(":",2);
//            if (c.length == 1)
//
        } else if (confName.contentEquals("missedcleavages")) {
            setMaxMissedCleavages(Integer.parseInt(confArgs.trim()));
        } else if (confName.contentEquals("topmgchits")) {
            int mgc =Integer.parseInt(confArgs.trim());
            setTopMGCHits(mgc);
            if (getTopMGXHits() < -1)
                setTopMGXHits(mgc);
        } else if (confName.contentEquals("topmgxhits")) {
            setTopMGXHits(Integer.parseInt(confArgs.trim()));
        } else if (confName.contentEquals("isotoppattern")) {
            Class i;
            try {
                i = Class.forName("rappsilber.ms.spectra.annotation." + confArgs);
                IsotopPattern ip;
                ip = (IsotopPattern) i.newInstance();
                setIsotopAnnotation(ip);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AbstractRunConfig.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(AbstractRunConfig.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(AbstractRunConfig.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (confName.contentEquals("mgcpeaks")) {
            int peaks = Integer.valueOf(confArgs);
            setNumberMGCPeaks(peaks);
        } else if (confName.contentEquals("custom")) {
           String[] customLines = confArgs.split("(\r?\n\r?|\\n)");
           for (int cl = 0 ; cl < customLines.length; cl++) {
               String tcl = customLines[cl].trim();
               if (!(tcl.startsWith("#") || tcl.isEmpty())) {
                    if (!evaluateConfigLine(tcl)) {
                        String[] parts = tcl.split(":", 2);
                        storeObject(parts[0].toUpperCase(), parts[1]);
                    }
               }
           }
        } else if (confName.contentEquals("maxpeakcandidates")) {
            setMaximumPeptideCandidatesPerPeak(Integer.parseInt(confArgs.trim()));
        } else if (confName.contentEquals("targetdecoy")) {
            String cl = confArgs.toLowerCase();
            if (cl.contentEquals("t") || cl.contentEquals("target"))
                m_decoyTreatment = SequenceList.DECOY_GENERATION.ISTARGET;
            else if (cl.contentEquals("rand") || cl.contentEquals("randomize"))
                m_decoyTreatment = SequenceList.DECOY_GENERATION.GENERATE_RANDOMIZED_DECOY;
            else if (cl.contentEquals("rev") || cl.contentEquals("reverse"))
                m_decoyTreatment = SequenceList.DECOY_GENERATION.GENERATE_REVERSED_DECOY;
//            int peaks = Integer.valueOf(confArgs);
//            setNumberMGCPeaks(peaks);
        }else if (confName.contentEquals("maxpeptidemass")){
            m_maxPeptideMass = Double.parseDouble(confArgs.trim());
        }else if (confName.contentEquals("evaluatelinears")){
            m_EvaluateLinears = getBoolean(confArgs,m_EvaluateLinears);
        }else if (confName.contentEquals("usecpus") || confName.contentEquals("searchthreads")){
            m_SearchThreads = calculateSearchThreads(Integer.parseInt(confArgs.trim()));
        } else if (confName.contentEquals("TOPMATCHESONLY".toLowerCase())) {
            m_topMatchesOnly = getBoolean(confArgs, false);
        } else if (confName.contentEquals("LOWRESOLUTION".toLowerCase())) {
            m_LowResolution = getBoolean(confArgs, false);
        } else if (confName.contentEquals("missing_isotope_peaks".toLowerCase())) {
            int p = Integer.parseInt(confArgs.trim());
            ArrayList<Double> setting = new ArrayList<Double>(p);
            for (int i =1; i<=p; i++) {
                setting.add(-i*Util.C13_MASS_DIFFERENCE);
            }
            if (setting.size() >0) {
                m_additionalPrecursorMZOffsets = setting;
                if (m_additionalPrecursorMZOffsetsUnknowChargeStates != null) {
                    for (Double mz : m_additionalPrecursorMZOffsets) {
                        m_additionalPrecursorMZOffsetsUnknowChargeStates.remove(mz);
                    }
                }
            }
        } else if (confName.contentEquals("missing_isotope_peaks_unknown_charge".toLowerCase())) {
            int p = Integer.parseInt(confArgs.trim());
            ArrayList<Double> setting = new ArrayList<Double>(p);
            for (int i =1; i<=p; i++) {
                setting.add(-i*Util.C13_MASS_DIFFERENCE);
            }
            if (setting.size() >0) {
                m_additionalPrecursorMZOffsetsUnknowChargeStates = setting;
                if (m_additionalPrecursorMZOffsets != null) {
                    for (Double mz : m_additionalPrecursorMZOffsets) {
                        m_additionalPrecursorMZOffsetsUnknowChargeStates.remove(mz);
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public void setMaxModifiedPeptidesPerPeptide(String confArgs) throws NumberFormatException {
        m_maxModifiedPeptidesPerPeptide = Integer.parseInt(confArgs.trim());
        if (!m_maxModifiedPeptidesPerFASTAPeptideSet)
            m_maxModifiedPeptidesPerFASTAPeptide = m_maxModifiedPeptidesPerPeptide;
    }

    public void setMaxModificationPerPeptide(String confArgs) throws NumberFormatException {
        m_maxModificationPerPeptide = Integer.parseInt(confArgs.trim());
        if (!m_maxModificationPerFASTAPeptideSet)
            m_maxModificationPerFASTAPeptide = m_maxModificationPerPeptide;
    }

    public void setMaxModificationPerFASTAPeptide(String confArgs) throws NumberFormatException {
        m_maxModificationPerFASTAPeptide = Integer.parseInt(confArgs.trim());
        m_maxModificationPerFASTAPeptideSet =true;
    }

    public void setMaxModifiedPeptidesPerFASTAPeptide(String confArgs) throws NumberFormatException {
        m_maxModifiedPeptidesPerFASTAPeptide = Integer.parseInt(confArgs.trim());
        m_maxModifiedPeptidesPerFASTAPeptideSet =true;
    }

    public int calculateSearchThreads(int searchThreads) {
        int realthreads;
        if (searchThreads == 0)
            realthreads = Math.max(Runtime.getRuntime().availableProcessors(),1);
        else if (searchThreads < 0 )
            realthreads = Math.max(Runtime.getRuntime().availableProcessors()+searchThreads,1);
        else
            realthreads = searchThreads;
        return realthreads;
    }

    public static boolean getBoolean(String value, boolean defaultValue) {
        if (value == null)
            return defaultValue;
        String v = value.trim();
        
        return ((String)v).contentEquals("true") ||
                        ((String)v).contentEquals("yes") ||
                        ((String)v).contentEquals("t") ||
                        ((String)v).contentEquals("y") ||
                        ((String)v).contentEquals("1");
    }

    public static boolean getBoolean(RunConfig config, String key, boolean defaultValue) {
        Object value = config.retrieveObject(key.toUpperCase());
        return getBoolean((String) value, defaultValue);
    }

    /**
     * @return the m_storedObjects
     */
    public HashMap<Object, Object> getStoredObjects() {
        return m_storedObjects;
    }

    /**
     * @param m_storedObjects the m_storedObjects to set
     */
    public void setStoredObjects(HashMap<Object, Object> m_storedObjects) {
        this.m_storedObjects = m_storedObjects;
    }


    public StringBuffer dumpConfig() {
        StringBuffer buf = new StringBuffer();
        for (String line : m_checkedConfigLines) {
            buf.append(line);
            buf.append("\n");
        }
        return buf;
    }

    public StatusInterface getStatusInterface() {
        return m_status;
    }

    public void setStatusInterface(StatusInterface i) {
        m_status_publishers.add(i);
    }

    public void addStatusInterface(StatusInterface i) {
        m_status_publishers.add(i);
    }

    public void removeStatusInterface(StatusInterface i) {
        m_status_publishers.remove(i);
    }
    

    public void clearStatusInterface() {
        m_status_publishers.clear();
    }
    
    protected XiProcess getXiSearch(Object sequences, AbstractSpectraAccess msmInput, ResultWriter output, StackedSpectraAccess sc, RunConfig conf, Class defaultClass) {
            //            xi = new SimpleXiProcessDevMGX(new File(fastaFile), sequences, output, conf, sc);
            //            xi = new SimpleXiProcessDev(new File(fastaFile), sequences, output, conf, sc);
            //            xi = new SimpleXiProcess(new File(fastaFile), sequences, output, conf, sc);
            XiProcess xi = null;
            String xiClassName = conf.retrieveObject("XICLASS", defaultClass.getName());
            Class xiclass;
            try {
                xiclass = Class.forName(xiClassName);
            } catch (ClassNotFoundException ex) {
                try {
                    xiclass = Class.forName("rappsilber.applications." + xiClassName);
                } catch (ClassNotFoundException ex2) {
                    xiclass = defaultClass;
                }
            }
            Constructor xiConstructor = null;
            try {
//                xiConstructor = xiclass.getConstructor(File.class, AbstractSpectraAccess.class, ResultWriter.class, RunConfig.class, StackedSpectraAccess.class);
                  xiConstructor = xiclass.getConstructor(sequences.getClass(), AbstractSpectraAccess.class, ResultWriter.class, RunConfig.class, StackedSpectraAccess.class);
            } catch (Exception ex) {
                if (sequences instanceof File) {
                    xi = new SimpleXiProcessLinearIncluded((File)sequences, msmInput, output, conf, sc);
                } else if (sequences instanceof File[]) {
                    xi = new SimpleXiProcessLinearIncluded((File[])sequences, msmInput, output, conf, sc);
                } else if (sequences instanceof SequenceList) {
                    xi = new SimpleXiProcessLinearIncluded((SequenceList)sequences, msmInput, output, conf, sc);
                }
            }

            if (xi == null) {
                try {
                    xi = (XiProcess) xiConstructor.newInstance(sequences, msmInput, output, conf, sc);
                } catch (Exception ex) {
                    if (sequences instanceof File) {
                        xi = new SimpleXiProcessLinearIncluded((File)sequences, msmInput, output, conf, sc);
                    } else if (sequences instanceof File[]) {
                        xi = new SimpleXiProcessLinearIncluded((File[])sequences, msmInput, output, conf, sc);
                    } else if (sequences instanceof SequenceList) {
                        xi = new SimpleXiProcessLinearIncluded((SequenceList)sequences, msmInput, output, conf, sc);
                    }
                }
            }
            return xi;
        }
    

    public void setLowResolution() {
        m_LowResolution = true;
    }
    
    public void setLowResolution(boolean lowresolution) {
        m_LowResolution = lowresolution;
    }
    
    public boolean isLowResolution() {
        if (m_LowResolution == null)
            return false;
        return m_LowResolution;
    }
    
    public SequenceList.DECOY_GENERATION getDecoyTreatment() {
        return m_decoyTreatment;
    }

    @Override
    public int getMaximumPeptideCandidatesPerPeak() {
        return m_maxFragmentCandidates;
    }
    
    public void setMaximumPeptideCandidatesPerPeak(int candidates) {
        m_maxFragmentCandidates = candidates;
    }
    
    
    public void setDecoyTreatment(SequenceList.DECOY_GENERATION dt) {
        m_decoyTreatment = dt;
    }

    /**
     * @return the m_maxPeptideMass
     */
    public double getMaxPeptideMass() {
        return m_maxPeptideMass;
    }

    /**
     * @param m_maxPeptideMass the m_maxPeptideMass to set
     */
    public void setMaxPeptideMass(double m_maxPeptideMass) {
        this.m_maxPeptideMass = m_maxPeptideMass;
    }

    /**
     * Also match linear peptides to spectra
     * @return the m_EvaluateLinears
     */
    public boolean isEvaluateLinears() {
        return m_EvaluateLinears;
    }

    /**
     * Also match linear peptides to spectra
     * @param m_EvaluateLinears the m_EvaluateLinears to set
     */
    public void setEvaluateLinears(boolean m_EvaluateLinears) {
        this.m_EvaluateLinears = m_EvaluateLinears;
    }

    /**
     * the number of concurent search threads
     * values &lt;0 should indicate that a according number of detected cpu-cores
     * should not be used.
     * E.g. if the computer has 16 cores and the setting is -1 then 15 search
     * threads should be started
     * otherwise the number of threads defined should be used.
     * @return the m_SearchThreads
     */
    public int getSearchThreads() {
        return m_SearchThreads;
    }

    /**
     * the number of concurent search threads
     * values &lt;0 should indicate that a according number of detected cpu-cores
     * should not be used.
     * E.g. if the computer has 16 cores and the setting is -1 then 15 search
     * threads should be started
     * otherwise the number of threads defined should be used.
     * @param m_SearchThreads the m_SearchThreads to set
     */
    public void setSearchThreads(int m_SearchThreads) {
        this.m_SearchThreads = calculateSearchThreads(m_SearchThreads);
    }

    /**
     * @return the m_topMatchesOnly
     */
    public boolean getTopMatchesOnly() {
        return m_topMatchesOnly;
    }

    /**
     * @param m_topMatchesOnly the m_topMatchesOnly to set
     */
    public void setTopMatchesOnly(boolean m_topMatchesOnly) {
        this.m_topMatchesOnly = m_topMatchesOnly;
    }
    
//    
//    public boolean getLowResolutionMatching() {
//        return m_lowResolutionMatching;
//    }
//
//    /**
//     * @param m_lowResolutionMatching the m_lowResolutionMatching to set
//     */
//    public void setLowResolutionMatching(boolean m_lowResolutionMatching) {
//        this.m_lowResolutionMatching = m_lowResolutionMatching;
//    }
    @Override
    public void addCrossLinkedFragmentProducer(CrossLinkedFragmentProducer producer, boolean isprimary) {
        m_crossLinkedFragmentProducer.add(producer);
        if (isprimary) {
            m_primaryCrossLinkedFragmentProducer.add(producer);
        }
    } 
    
    @Override
    public ArrayList<CrossLinkedFragmentProducer> getCrossLinkedFragmentProducers() {
        return m_crossLinkedFragmentProducer;
    }

    @Override
    public ArrayList<CrossLinkedFragmentProducer> getPrimaryCrossLinkedFragmentProducers() {
        return m_primaryCrossLinkedFragmentProducer;
    }
    
    
    /**
     * A list of precursor m/z offset that each spectrum should be searched with
     * @return 
     */
    @Override
    public ArrayList<Double> getAdditionalPrecursorMZOffsets() {
        return m_additionalPrecursorMZOffsets;
    }

    /**
     * A list of precursor m/z offset that each spectrum with unknown precursor 
     * charge state should be searched with
     * @return 
     */
    @Override
    public ArrayList<Double> getAdditionalPrecursorMZOffsetsUnknowChargeStates() {
        return m_additionalPrecursorMZOffsetsUnknowChargeStates;
    }


    public int getMaximumModificationPerPeptide() {
        return m_maxModificationPerPeptide;
    }

    public int getMaximumModifiedPeptidesPerPeptide() {
        return m_maxModifiedPeptidesPerPeptide;
    }
    //rappsilber.utils.Util.MaxModificationPerPeptide = m_config.retrieveObject("MAX_MODIFICATION_PER_PEPTIDE", rappsilber.utils.Util.MaxModificationPerPeptide);

    public int getMaximumModificationPerFASTAPeptide() {
        return m_maxModificationPerFASTAPeptide;
    }

    public int getMaximumModifiedPeptidesPerFASTAPeptide() {
        return m_maxModifiedPeptidesPerFASTAPeptide;
    }

    
    @Override
    public boolean searchStoped() {
        return m_searchStoped;
    }

    @Override
    public void stopSearch() {
        m_searchStoped  =true;
    }
    
}
