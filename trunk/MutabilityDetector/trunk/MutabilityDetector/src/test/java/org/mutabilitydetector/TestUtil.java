/* 
 * Mutability Detector
 *
 * Copyright 2009 Graham Allan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.mutabilitydetector;

import static java.util.Arrays.asList;
import static org.mutabilitydetector.MutabilityReason.PUBLISHED_NON_FINAL_FIELD;

import java.util.Collection;

import org.junit.Ignore;
import org.mutabilitydetector.IAnalysisSession.IsImmutable;
import org.mutabilitydetector.unittesting.MutabilityAssert;

@Ignore
public class TestUtil {
	public static IsImmutable getIsImmutableResult(Class<?> toAnalyse) {
		IsImmutable result = new AnalysisSession().isImmutable(toAnalyse.getName());
		return result;
	}
	
	public static AnalysisResult getAnalysisResult(Class<?> toAnalyse) {
		return new AnalysisSession().resultFor(toAnalyse.getName());
	}
	
	public static String formatReasons(Collection<CheckerReasonDetail> reasons) {
		return MutabilityAssert.formatReasons(reasons);
	}

	public static Collection<CheckerReasonDetail> unusedCheckerReasonDetails() {
		return asList(new CheckerReasonDetail("this reason is not meant to be involved", null, PUBLISHED_NON_FINAL_FIELD));
	}
}