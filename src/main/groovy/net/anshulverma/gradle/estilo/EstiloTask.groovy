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
package net.anshulverma.gradle.estilo

import groovy.transform.TypeChecked
import net.anshulverma.gradle.estilo.checkstyle.checks.ConfigFileLoader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * @author Anshul Verma (anshul.verma86@gmail.com)
 */
@TypeChecked
class EstiloTask extends DefaultTask {

  private String checkstyleConfigDir

  EstiloTask() {
    description = 'Manages checkstyle checkers for this project.'
    group = 'Checkstyle'
  }

  @TaskAction
  def run() {
    def project = getProject()
    def settings = (EstiloExtension) project.getExtensions().findByName('estilo')
    new ConfigFileLoader(settings.baseChecks, getCheckstyleConfigDir()).load()
  }

  String getCheckstyleConfigDir() {
    System.properties.get('checkstyleConfigDir', checkstyleConfigDir)
  }

  def setCheckstyleConfigDir(String checkstyleConfigDir) {
    this.checkstyleConfigDir = checkstyleConfigDir
  }
}
