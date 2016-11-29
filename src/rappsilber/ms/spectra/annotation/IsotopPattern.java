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
package rappsilber.ms.spectra.annotation;

import rappsilber.ms.sequence.Peptide;
import rappsilber.ms.spectra.SpectraPeak;
import rappsilber.ms.spectra.Spectra;
import rappsilber.ms.spectra.SpectraPeakCluster;
import rappsilber.ms.ToleranceUnit;
import rappsilber.utils.SortedLinkedList;
import rappsilber.utils.Util;

/**
 *
 * @author Lutz Fischer <l.fischer@ed.ac.uk>
 */
public class IsotopPattern extends Anotation{

    private ToleranceUnit m_Tolerance = null;
    private double        m_MaxMono2FirstPeakRatio = 8;
    private double        m_MaxPeakToPeakRation    = Double.MAX_VALUE;

    /**
     * annotates all isotope-cluster as such. <br/>
     * Each peak of an cluster gets annotated as SpectraPeakAnnotation.isotope
     * and the mono isotopic peak additionally as SpectraPeakAnnotation.monoisotopic
     * @param spectra
     * @param MaxCharge
     */
    public void AnnotateIsotops (Spectra spectra, int MaxCharge) {
        SortedLinkedList<SpectraPeakCluster> isotopClusters = spectra.getIsotopeClusters();

        // don't exced the precurser charge
        MaxCharge = Math.min(MaxCharge, (int)spectra.getPrecurserCharge());

        // for faster handling get the peaks as array
        SpectraPeak[] peaks = spectra.getPeaksArray();

        int peakCount = peaks.length;

        // check each peak, whether it's a start of a isotop cluster
        for (int i = 0;i< peakCount - 1;i++) {
            if (peaks[i].hasAnnotation(SpectraPeakAnnotation.isotop))
                continue;

            // only consider peaks, that have no more then a distance of 1 (cluster for singly charged ions)
            if (spectra.getTolearance().minDiff(peaks[i].getMZ(), peaks[i + 1].getMZ()) <= 1.0){

                int lastPeak = Math.min(peakCount, i + Util.IsotopClusterMaxPeaks);

                // TDOD:cleanup
                if ((int)(peaks[i].getMZ()) == 998)
                    lastPeak +=0;


                // check for each charge
                charge: for (int charge = 1; charge <= MaxCharge; charge++) {
                    SpectraPeakCluster spc = new SpectraPeakCluster(spectra.getTolearance());
                    spc.add(peaks[i]);
                    double diff = Util.C13_MASS_DIFFERENCE/charge;
                    SpectraPeak monoPeak = peaks[i];
                    double pMZ = monoPeak.getMZ();
//                    double next = pMZ + diff;
                    double nextDiff = diff;
                    SpectraPeak sp = spectra.getPeakAtDistance(monoPeak, nextDiff);
                    //double ratio = sp.getIntensity()/peaks[i].getIntensity();
                    if (sp != null && sp.getIntensity()/peaks[i].getIntensity() < getMaxMono2FirstPeakRatio()) {
                        double spcNext  = nextDiff;
                        do {
                            spc.add(sp);
                            sp.annotate(SpectraPeakAnnotation.isotop);
                            sp.setCharge(charge);

                            spcNext+=diff;
                        } while ((sp = spectra.getPeakAtDistance(monoPeak, spcNext)) != null);


                        monoPeak.setCharge(charge);
                        monoPeak.annotate(SpectraPeakAnnotation.isotop);
                        monoPeak.annotate(SpectraPeakAnnotation.monoisotop);

                        spc.setMonoIsotopic(monoPeak);
                        spc.setCharge(charge);
                        spc.setMZ(pMZ);

                        isotopClusters.add(spc);

                        double prev = - diff;

                        // maybe some of the previuos peaks were masked by a previous cluster
                        if ((sp = spectra.getPeakAtDistance(monoPeak, prev)) != null) {
                            spc = (SpectraPeakCluster) spc.clone();

                            SpectraPeak cMono =monoPeak;
                            do {
                                spc.add(0,sp);
                                sp.annotate(SpectraPeakAnnotation.isotop);
                                sp.setCharge(charge);
                                cMono = sp;
                                prev-=diff;
                            } while ((sp = spectra.getPeakAtDistance(monoPeak, prev)) != null);

                            cMono.setCharge(charge);
//                            cMono.annotate(SpectraPeakAnnotation.isotop);
                            cMono.annotate(SpectraPeakAnnotation.monoisotop);

                            spc.setMonoIsotopic(cMono);
                            spc.setCharge(charge);
                            spc.setExtended(true);
//                            spc.setMZ(cMono.getMZ());

                            isotopClusters.add(spc);

                        }
                    }

//                    // mass difference for that charge
//                    double diff = 1.0 / (double)charge;
//
//                    // find n equidistant peaks in a row, that have a distance of 1, 0.5, 0.25 ...
//                    SpectraPeakCluster currentPattern = null;
//                    int j;
//                    boolean foundCluster = false;
//                    for (j = i + 1; j < lastPeak; j++) {
//                        if (spectra.getPeakAt())
//
//                        if (spectra.getTolearance().compare(peaks[j].getMZ() - diff, peaks[j - 1].getMZ()) != 0 || // distance matches charge
//                                peaks[j - 1 ].getIntensity() / peaks[j].getIntensity() > Util.IsotopClusterMaxYRation || // maximal change from one peak to the next
//                                peaks[i].getIntensity() / peaks[j].getIntensity() > 2*Util.IsotopClusterMaxYRation) {  // maximal change to the first peak
//                            break;
//                        }
//
//                        foundCluster = true;
//
//                        // mark peak as isotop
//                        peaks[j].annotate(SpectraPeakAnnotation.isotop);
//                        // we know the charge by now
//                        peaks[j].setCharge(charge);
//
//
//                        if (j == i +1) {
//
//                            // and the first as monoisotopic, might be corrected later on
//                            peaks[i].annotate(SpectraPeakAnnotation.isotop);
//                            peaks[i].annotate(SpectraPeakAnnotation.monoisotop);
//                            peaks[i].setCharge(charge);
//                            currentPattern = new SpectraPeakCluster();
//                            isotopClusters.add(currentPattern);
//                            currentPattern.add(peaks[i]);
//
//                        }
//                        currentPattern.add(peaks[j]);
//                    }
//
//                    // don't look at the same peaks again
//                    if (foundCluster)
//                        i = j-1;

                }
            }
            

        }
       // javax.swing.JOptionPane.showMessageDialog(null, isotopClusters.size());
    }

    public void anotate(Spectra s) {
        AnnotateIsotops(s, (int)s.getPrecurserCharge());
    }

    @Override
    public void anotate(Spectra s, Peptide Peptide) {
        anotate(s);
    }

    @Override
    public void anotate(Spectra s, Peptide Peptide1, Peptide Peptide2) {
        anotate(s);
    }

    /**
     * @return the m_MaxMono2FirstPeakRatio
     */
    public double getMaxMono2FirstPeakRatio() {
        return m_MaxMono2FirstPeakRatio;
    }

    /**
     * @param MaxMono2FirstPeakRatio the MaxMono2FirstPeakRatio to set
     */
    public void setMaxMono2FirstPeakRatio(double MaxMono2FirstPeakRatio) {
        this.m_MaxMono2FirstPeakRatio = MaxMono2FirstPeakRatio;
    }

    /**
     * @return the MaxPeakToPeakRation
     */
    public double getMaxPeakToPeakRation() {
        return m_MaxPeakToPeakRation;
    }

    /**
     * @param MaxPeakToPeakRation the m_MaxPeakToPeakRation to set
     */
    public void setMaxPeakToPeakRation(double MaxPeakToPeakRation) {
        this.m_MaxPeakToPeakRation = MaxPeakToPeakRation;
    }


}
