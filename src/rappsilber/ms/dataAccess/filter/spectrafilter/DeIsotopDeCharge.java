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
package rappsilber.ms.dataAccess.filter.spectrafilter;

import rappsilber.config.RunConfig;
import rappsilber.ms.dataAccess.AbstractStackedSpectraAccess;
import rappsilber.ms.spectra.Spectra;
import rappsilber.ms.spectra.annotation.Averagin;

/**
 *
 * @author Lutz Fischer <l.fischer@ed.ac.uk>
 */
public class DeIsotopDeCharge extends AbstractStackedSpectraAccess {
    Spectra s = null;
//    SpectraAccess innerreader = null;
    Averagin a;
//    ToleranceUnit t = new ToleranceUnit("10ppm");

    public DeIsotopDeCharge(RunConfig conf) {
        a = new Averagin(conf);
    }

    public DeIsotopDeCharge(RunConfig conf,String settings) {
        this(conf);
    }



    @Override
    public Spectra current() {
        return s;
    }

    @Override
    public int countReadSpectra() {
        return m_InnerAcces.countReadSpectra();
    }



    @Override
    public Spectra next() {
        synchronized (m_sync) {
            s = m_InnerAcces.next();
            a.AnnotateIsotops(s, s.getPrecurserCharge());
            s = s.deChargeDeisotop();
            return s;
        }
    }





}
