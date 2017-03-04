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
package rappsilber.ms.sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;
import rappsilber.config.RunConfig;
import rappsilber.ms.crosslinker.CrossLinker;
import rappsilber.ms.dataAccess.filter.fastafilter.FastaFilter;
import rappsilber.ms.dataAccess.filter.fastafilter.MultiFilter;
import rappsilber.ms.dataAccess.filter.fastafilter.NoFilter;
import rappsilber.ms.lookup.peptides.PeptideLookup;
import rappsilber.ms.sequence.Iterators.FragmentIterator;
import rappsilber.ms.sequence.Iterators.PeptideIterator;
import rappsilber.ms.sequence.digest.Digestion;
import rappsilber.ms.sequence.ions.Fragment;

/**
 *
 * @author Lutz Fischer <l.fischer@ed.ac.uk>
 */
public class SequenceList extends ArrayList<Sequence> {
    private static final long serialVersionUID = -1777435154539589179L;

    private int     m_countPeptides = 0;
    private int     m_countModifiedPeptides = 0;
    private Peptide[] m_AllPeptides = null;
    private RunConfig m_config = null;
    
    private boolean m_hasDecoys = false;
    
    private FastaFilter m_filter = new NoFilter();
    
    public static enum DECOY_GENERATION {
        ISTARGET,ISDECOY,GENERATE_REVERSED_DECOY,GENERATE_RANDOMIZED_DECOY
    }
    
    private DECOY_GENERATION m_decoyTreatment = DECOY_GENERATION.ISTARGET;



    private class SequenceFragmentIterator implements FragmentIterator {
            PeptideIterator m_peptides = peptides();
            Iterator<Fragment> m_current = new Iterator<Fragment>() {
                public boolean hasNext() {
                    return false;
                }
                public Fragment next() {throw new UnsupportedOperationException("Not supported yet.");}
                public void remove() {throw new UnsupportedOperationException("Not supported yet.");}
            };
            Peptide  m_currentPeptide;

            int m_peptide = 0;

            public boolean hasNext() {
                return m_peptides.hasNext() || (m_current.hasNext());
            }

            public Fragment next() {
                if (!m_current.hasNext()) {
                    m_currentPeptide = m_peptides.next();
                    //System.out.println("Peptide: " + m_currentPeptide);
                    m_current = m_currentPeptide.getPrimaryFragments().iterator();
                }
                return m_current.next();
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Sequence getCurrentSequence() {
                return peptides().getCurrentSequence();
            }

            public Peptide getCurrentPeptide() {
                return m_currentPeptide;
            }

    }

    private class ConfiguresSequenceFragmentIterator implements FragmentIterator {
            PeptideIterator m_peptides = peptides();
            Iterator<Fragment> m_current = new Iterator<Fragment>() {
                public boolean hasNext() {
                    return false;
                }
                public Fragment next() {throw new UnsupportedOperationException("Not supported yet.");}
                public void remove() {throw new UnsupportedOperationException("Not supported yet.");}
            };
            Peptide  m_currentPeptide;
            RunConfig m_conf;

             ConfiguresSequenceFragmentIterator(RunConfig conf) {
                 m_conf = conf;
             }

            int m_peptide = 0;

            public boolean hasNext() {
                return m_peptides.hasNext() || (m_current.hasNext());
            }

            public Fragment next() {
                if (!m_current.hasNext()) {
                    m_currentPeptide = m_peptides.next();
                    //System.out.println("Peptide: " + m_currentPeptide);
                    m_current = m_currentPeptide.getPrimaryFragments(m_conf).iterator();
                }
                return m_current.next();
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Sequence getCurrentSequence() {
                return peptides().getCurrentSequence();
            }

            public Peptide getCurrentPeptide() {
                return m_currentPeptide;
            }

    }

    private class SequenceSublistIterator implements Iterator<SequenceList> {
        int first = 0;
        int targetSize;

        public SequenceSublistIterator(int size) {
            targetSize = size;
        }

        public boolean hasNext() {
            return first < size();
        }

        public SequenceList next() {
            SequenceList l = new SequenceList(m_decoyTreatment, targetSize, m_config);
            int end = Math.min(first + targetSize, size());
            for (int s = first; s<end; s++) {
                l.add(get(s));
            }
            return l;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }


    public SequenceList(RunConfig config) {
        this(config == null?DECOY_GENERATION.ISTARGET : config.getDecoyTreatment(), config);
    }
    
    public SequenceList(DECOY_GENERATION decoys, RunConfig config) {
        this(decoys);
        m_config = config;
    }

    private SequenceList(DECOY_GENERATION decoys) {
        m_decoyTreatment = decoys;
    }

    private SequenceList(DECOY_GENERATION decoys,File FastaFile) throws IOException {
        this(decoys);
        addFasta(FastaFile, decoys);
    }

    public SequenceList(DECOY_GENERATION decoys,File FastaFile,RunConfig config) throws IOException {
        this(decoys);
        m_config = config;
        addFasta(FastaFile, decoys);
    }

    public SequenceList(DECOY_GENERATION decoys,File FastaFile, FastaFilter filter, RunConfig config) throws IOException {
        this(decoys);
        m_config = config;
        addFilter(filter);
        addFasta(FastaFile, decoys);
    }
    
//    private SequenceList(DECOY_GENERATION decoys,File[] FastaFile) throws IOException {
//        this(decoys);
//        for (File f : FastaFile)
//            addFasta(f, decoys);
//    }

    public SequenceList(DECOY_GENERATION decoys,File[] FastaFile,RunConfig config) throws IOException {
        this(decoys);
        m_config = config;
        for (File f : FastaFile)
            addFasta(f, decoys);
    }

    public SequenceList(File[] FastaFile,RunConfig config) throws IOException {
        this(config.getDecoyTreatment(), FastaFile, config);
    }
    
    private SequenceList(DECOY_GENERATION decoys,BufferedReader FastaFile) throws IOException {
        this(decoys);
        addFasta(FastaFile, decoys);
    }



    public SequenceList(DECOY_GENERATION decoys,BufferedReader FastaFile,RunConfig config) throws IOException {
        this(decoys);
        m_config = config;
        addFasta(FastaFile, decoys);
    }

    private SequenceList(DECOY_GENERATION decoys, int capacity) {
        super(capacity);
        m_decoyTreatment = decoys;
    }

    public SequenceList(DECOY_GENERATION decoys, int capacity,RunConfig config) { //throws IOException {
        this(decoys, capacity);
        m_config = config;
    }

    public int digest(Digestion method, ArrayList<CrossLinker> cl) {
        return digest(method, Double.MAX_VALUE - 1, cl);
    }


    public int digest(Digestion method, double maxMass, ArrayList<CrossLinker> cl) {
        int countPeptides = 0;
        Iterator<Sequence> list = iterator();
        while (list.hasNext()) {
            countPeptides += list.next().digest(method, maxMass, cl);
        }
        m_countPeptides = countPeptides;
        m_config.getStatusInterface().setStatus("Digest: Peptides: " + countPeptides );
        return countPeptides;
    }


//    public int fragment(Digestion digest) {
//        int countFragments = 0;
//        Iterator<Peptide> peps = peptides();
//        while (peps.hasNext()) {
//            countFragments += peps.next().fragmentPrimary();
//        }
//        return countFragments;
//    }


    public void buildIndex() {
        //m_AllPeptides = new Peptide[m_countPeptides];
        ArrayList<Peptide> all = new ArrayList<Peptide>(m_countPeptides);
        PeptideIterator peps = peptides();
        int i = 0;
        try {
            while (peps.hasNext()) {
                Peptide p = peps.next();
                //m_AllPeptides[i] = peps.next();
                if (p != null) {
                    p.setPeptideIndex(i);
                    i++;
                    all.add(p);
                }
               
            }
            m_AllPeptides = new Peptide[all.size()];
            all.toArray(m_AllPeptides);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public void dumpPeptides() {
        if (m_AllPeptides != null || m_AllPeptides.length == 0)
            buildIndex();
        for (Peptide p : m_AllPeptides) {
            System.out.println(p.toString() + "  " + p.getMass());
        }
    }

    public int applyVariableModifications() {
        Iterator<Sequence> sIt = this.iterator();
        while (sIt.hasNext()) {
            Sequence s = sIt.next();
            Iterator<Peptide> pIt = s.getPeptides().iterator();
            m_countModifiedPeptides += s.modify();
        }
        m_countPeptides += m_countModifiedPeptides;
        return m_countModifiedPeptides;
    }

    public int applyVariableModifications(RunConfig conf, PeptideLookup lookup, ArrayList<CrossLinker> linkers, Digestion enzym) {
        Iterator<Sequence> sIt = this.iterator();
        int mod=0;
        while (sIt.hasNext()) {
            Sequence s = sIt.next();
            Iterator<Peptide> pIt = s.getPeptides().iterator();
            m_countModifiedPeptides += s.modify(conf, lookup, linkers, enzym);
            if (++mod % 1000 == 0) {
                conf.getStatusInterface().setStatus("Variable modification: Peptides: " + m_countModifiedPeptides + " total:" +( m_countPeptides + m_countModifiedPeptides));
            }
        }
        m_countPeptides += m_countModifiedPeptides;
        return m_countModifiedPeptides;
    }

    /**
     * creates an iterator over all existing peptides in the SequenceList
     * @return the Iterator
     */
    public PeptideIterator peptides () {
        return new PeptideIterator() {

            Sequence m_currentSequence;
            int sCount = 0;

            Iterator<Sequence> m_sequences = iterator();

            Iterator<Peptide> m_current = new Iterator<Peptide>() {
                public boolean hasNext() {
                    return false;
                }
                public Peptide next() {throw new UnsupportedOperationException("Not supported yet.");}
                public void remove() {throw new UnsupportedOperationException("Not supported yet.");}
            };

            Peptide m_currentPeptide = null;

            int m_peptide = 0;

            public boolean hasNext() {
                return m_sequences.hasNext() || (m_current.hasNext());
            }

            public synchronized Peptide next() {
                if (!m_current.hasNext()) {
                    sCount ++;

                    m_currentSequence = m_sequences.next();
                    m_current = m_currentSequence.getPeptides().iterator();
                }
                m_currentPeptide = m_current.hasNext() ? m_current.next(): null;
                return (m_currentPeptide);
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Sequence getCurrentSequence() {
                return m_currentSequence;
            }

            @Override
            public Peptide current() {
                return m_currentPeptide;
            }
        };
    }

    /**
     * creates an iterator over all existing fragments in the SequenceList
     * @return the Iterator
     */
    public FragmentIterator fragments (RunConfig conf) {

        return new ConfiguresSequenceFragmentIterator(conf);
    }

    /**
     * creates an iterator over all existing fragments in the SequenceList
     * @return the Iterator
     */
    public FragmentIterator fragments () {
        if (m_config == null)
            return new SequenceFragmentIterator();  
        else
            return new ConfiguresSequenceFragmentIterator(m_config);
    }

    /**
     * @return the m_AllPeptides
     */
    public Peptide[] getAllPeptideIDs() {
        if (m_AllPeptides == null)
            buildIndex();
        return m_AllPeptides;
    }

    public Peptide getPeptide(int index) {
        return m_AllPeptides[index];
    }

    public Sequence getSequence(int peptide) {
        return m_AllPeptides[peptide].getSequence();
    }

    public void applyFixedModifications(AminoModification[] modifications) {
        for (Sequence s : this) {
            s.replace(modifications);
        }
    }

    public void applyFixedModifications(ArrayList<AminoModification> modifications) {
        AminoModification[] am = new AminoModification[0];
        am = modifications.toArray(am);
        applyFixedModifications(am);
    }

    public void applyFixedModifications() {
        for (Sequence s : this) {
            s.applyFixedModifications();
        }
    }

    public void applyLabel(RunConfig conf) {
        ArrayList<Sequence> allLabled = new ArrayList<Sequence>();
        boolean labeled =false;
        for (Sequence s : this) {
            Sequence labled = new Sequence(s, 0, s.length());
            checkLabel : for (AminoLabel al : conf.getLabel()) {
                if (s.containsAminoAcid(al.BaseAminoAcid)) {
                    for (AminoLabel alrep : conf.getLabel())
                        labled.replace(alrep.BaseAminoAcid, alrep);
                    labeled = true;
                }
            }
            if (labeled)
                allLabled.add(labled);
        }
        this.addAll(allLabled);
//        for (Sequence s : this) {
//            s.label(conf);
//        }
    }

    public void applyFixedModifications(RunConfig conf) {
        for (Sequence s : this) {
            s.applyFixedModifications(conf);
        }
    }

    /**
     * @return the m_countPeptides
     */
    public int getCountPeptides() {
        return m_countPeptides;
    }


    public SequenceList getRandomSubList(int count){
        SequenceList newList = new SequenceList(m_decoyTreatment, count);
        LinkedList<Sequence> oldList = new LinkedList(this);
        HashSet<Sequence> hashSet = new HashSet<Sequence>(count);
        int np = 0;
        while (np<count && oldList.size() > 0) {
            int op = (int)(Math.random() * oldList.size());
            newList.add(oldList.remove(op));
            np++;
        }
        return newList;
    }

    public Iterator<SequenceList> getSublists(int size) {

        return new SequenceSublistIterator(size);
    }


    /**
     * include reversed sequences as decoys
     * @return returns an iterator of all decoy sequences
     */
    public ArrayList<Sequence> includeReverse () {
        ArrayList<Sequence> decoys = new ArrayList<Sequence>(size());
        for (Sequence s : this) {
            Sequence ds = s.reverse();
            ds.setDecoy(true);
            decoys.add(ds);
        }
        this.addAll(decoys);
        return decoys;

    }
    
    /**
     * include reversed sequences as decoys
     * @return returns an iterator of all decoy sequences
     */
    public ArrayList<Sequence> includeReverseAndSwap (HashSet<AminoAcid> aas) {
        ArrayList<Sequence> decoys = new ArrayList<Sequence>(size());
        for (Sequence s : this) {
            Sequence ds = s.reverse();
            ds.swapWithPredecesor(aas);
            ds.setDecoy(true);
            decoys.add(ds);
        }
        this.addAll(decoys);
        return decoys;

    }
    
    /**
     * include reversed sequences as decoys
     * @return returns an iterator of all decoy sequences
     */
    public ArrayList<Sequence> includeReverseKRAvera () {
        ArrayList<Sequence> decoys = new ArrayList<Sequence>(size());
        for (Sequence s : this) {
            Sequence ds = s.reverseAvare(new AminoAcid[]{AminoAcid.K,AminoAcid.R});
            ds.setDecoy(true);
            decoys.add(ds);
        }
        this.addAll(decoys);
        return decoys;

    }
    
    /**
     * include reversed sequences as decoys
     * @return returns an iterator of all decoy sequences
     */
    public ArrayList<Sequence> includeShuffled () {
        ArrayList<Sequence> decoys = new ArrayList<Sequence>(size());
        for (Sequence s : this) {
            Sequence ds = s.shuffle();
            ds.setDecoy(true);
            decoys.add(ds);
        }
        this.addAll(decoys);
        return decoys;

    }

    /**
     * include reversed sequences as decoys
     * @return returns an iterator of all decoy sequences
     */
    public ArrayList<Sequence> includeShuffled (HashSet<AminoAcid> nonShuffledAAs) {
        ArrayList<Sequence> decoys = new ArrayList<Sequence>(size());
        for (Sequence s : this) {
            Sequence ds = s.shuffle(nonShuffledAAs);
            ds.setDecoy(true);
            decoys.add(ds);
        }
        this.addAll(decoys);
        return decoys;
    }
    
    
    public void addFasta(File FastaFile) throws IOException {
        addFasta(FastaFile, m_decoyTreatment);
        
    }
    public void addFasta(File FastaFile, DECOY_GENERATION decoy) throws IOException {
        String filename = FastaFile.getName().toLowerCase();
        if (filename.endsWith("fastalist") || filename.endsWith("list"))
            addFastaList(FastaFile);
        else {
            GZIPInputStream gzipIn = null;
            try {
                gzipIn =  new GZIPInputStream(new FileInputStream(FastaFile));
            } catch (Exception e) {
                
            }
            if (gzipIn == null) {
                addFasta(new BufferedReader(new FileReader(FastaFile)), decoy);
            } else 
                addFasta(new BufferedReader(new InputStreamReader(gzipIn)), decoy);
        }
    }

    public void addFastaList(File FastaFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(FastaFile));
        String line = null;
        while ((line = br.readLine())!= null) {
            DECOY_GENERATION decoy = DECOY_GENERATION.ISTARGET;
            line = line.trim();
            if (line.length() > 0 &&  ! line.startsWith("#")) {
                if (line.toLowerCase().startsWith("decoy:")) {
                    decoy = DECOY_GENERATION.ISDECOY;
                    line = line.substring(6).trim();
                } else if (line.toLowerCase().startsWith("target:")) {
                    decoy = DECOY_GENERATION.ISTARGET;
                    line = line.substring(6).trim();
                }
                File fasta = new File(line);
                if (!fasta.exists()) {
                    String thisParent = FastaFile.getParent();
                    if (thisParent == null)
                        throw new FileNotFoundException("could not find referenced MSM-file: " + line);
                    fasta = new File(thisParent + File.separator + line);
                }
                addFasta(fasta, decoy);
            }
        }
    }

    public void addFasta(InputStream FastaFile, DECOY_GENERATION decoy) throws IOException {
        addFasta(new BufferedReader(new InputStreamReader(FastaFile)), decoy);
    }


    public void addFasta(BufferedReader FastaFile, DECOY_GENERATION decoy) throws IOException {
        StringBuilder s = null;
        String FastaHeader = null;
        m_hasDecoys = m_hasDecoys || decoy != DECOY_GENERATION.ISTARGET;
        while (FastaFile.ready()) {
            String line = FastaFile.readLine();
            if (line == null)
                break;
            line = line.trim();
            if (line.length() > 0) {
                if (line.charAt(0) == '>') {
                    if (s != null) {
                        if (s.subSequence(s.length()-1, s.length()).toString().contentEquals("*"))
                            s.setLength(s.length() - 1);
                        Sequence seq = new Sequence(s.toString(), FastaHeader, m_config);
                        seq.setDecoy(decoy==DECOY_GENERATION.ISDECOY);
                        
                        Sequence[] toAdd = m_filter.getSequences(seq);
                        
                        for (Sequence a : toAdd ) {
                            this.add(a);
                            if (decoy == DECOY_GENERATION.GENERATE_REVERSED_DECOY) {
                                this.add(a.reverse());
                            } else if (decoy == DECOY_GENERATION.GENERATE_RANDOMIZED_DECOY) {
                                this.add(a.shuffle());
                            }
                        }

                    }
                    FastaHeader = line.substring(1);
                    s = new StringBuilder();
                } else if (line.length() > 0) {
                    s.append(line);
                }
            }
        }
        if (s != null && s.length() > 0) {
            // delete a trailing *
            if (s.subSequence(s.length()-1, s.length()).toString().contentEquals("*"))
                s.setLength(s.length() - 1);

            Sequence seq = new Sequence(s.toString(), FastaHeader, m_config);
            seq.setDecoy(decoy==DECOY_GENERATION.ISDECOY);
            
            Sequence[] toAdd = m_filter.getSequences(seq);

            for (Sequence a : toAdd ) {
                this.add(seq);
                if (decoy == DECOY_GENERATION.GENERATE_REVERSED_DECOY) {
                    this.add(seq.reverse());
                } else if (decoy == DECOY_GENERATION.GENERATE_RANDOMIZED_DECOY) {
                    this.add(seq.shuffle());
                }
            }
        }
    }

    
    public void addFilter(FastaFilter ff) {
        if (m_filter instanceof NoFilter) {
            m_filter = ff;
        } else {
            if (m_filter instanceof MultiFilter) {
                ((MultiFilter) m_filter).addFilter(ff);
            } else {
                MultiFilter mf = new MultiFilter();
                mf.addFilter(m_filter);
                mf.addFilter(ff);
                m_filter = mf;
            }
        }
    }
    
    public boolean hasDecoy() {
        return m_hasDecoys;
    }
}
