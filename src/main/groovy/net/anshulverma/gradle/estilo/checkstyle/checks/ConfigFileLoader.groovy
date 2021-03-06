/**
 * Copyright 2015 Anshul Verma. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.anshulverma.gradle.estilo.checkstyle.checks

import groovy.transform.TupleConstructor
import groovy.transform.TypeChecked
import net.anshulverma.gradle.estilo.checkstyle.config.CheckstyleConfig
import net.anshulverma.gradle.estilo.checkstyle.config.ConfigMarshaller
import org.apache.commons.io.FileUtils

import java.nio.file.Paths

/**
 * @author Anshul Verma (anshul.verma86@gmail.com)
 */
@TypeChecked
@TupleConstructor
class ConfigFileLoader {

  CheckType checkType

  CheckstyleConfig load() {
    if(checkType == CheckType.CUSTOM){
      loadUrl(new File(checkType.filename).toURI().toURL())
    }else{
      loadUrl(this.getClass().getResource("/${checkType.filename}"))
    }
  }

  private CheckstyleConfig loadUrl(URL url){
    File tmpFile = File.createTempFile('checkstyle', Paths.get(checkType.filename).getFileName().toString())
    FileUtils.copyURLToFile(url, tmpFile)
    ConfigMarshaller.INSTANCE.unmarshal(tmpFile)
  }
}
