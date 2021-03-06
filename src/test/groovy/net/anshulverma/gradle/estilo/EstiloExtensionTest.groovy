/**
 * Copyright 2015 Anshul Verma. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.anshulverma.gradle.estilo

import net.anshulverma.gradle.estilo.checkstyle.checks.CheckType
import net.anshulverma.gradle.estilo.test.AbstractSpecification
import spock.lang.Unroll

/**
 * @author Anshul Verma (anshul.verma86@gmail.com)
 */
class EstiloExtensionTest extends AbstractSpecification {

  @Unroll
  def 'check type #checkType named #name selection test'() {
    given:
      def extension = new EstiloExtension()
      def closure = {
        source name
      }
      closure.delegate = extension

    when:
      closure()

    then:
      extension.baseChecks == checkType

    where:
      checkType        | name
      CheckType.GOOGLE | 'google'
      CheckType.SUN    | 'sun'
      CheckType.EMPTY  | 'empty'
      CheckType.CUSTOM | 'custom'
  }

  def 'Allow custom baseChecks'(){
    given:
      def extention = new EstiloExtension()
      def closure = {
        customSource checkstylePath
      }
      closure.delegate = extention

    when:
      closure()

    then:
      extention.baseChecks.filename == checkstylePath

    where:
      checktype                                 | checkstylePath
      CheckType.CUSTOM                          | 'config/checkstyle.xml'
      CheckType.setCustom('/path/to/somewhere') | '/path/to/somewhere'

  }

  def 'Allow adding RegexpHeader check'() {
    given:
      def extension = new EstiloExtension()
      def rootDir = '/tmp'
      def closure = {
        checks {
          RegexpHeader {
            headerFile "$rootDir/config/checkstyle/java.header.txt"
            multiLines([23, 24, 25])
          }
        }
      }
      closure.delegate = extension

    when:
      closure()

    then:
      extension.checkCollection.length == 1

      def check = extension.checkCollection.collection.pop()
      check.name == 'RegexpHeader'
      check.headerFile == '/tmp/config/checkstyle/java.header.txt'
      check.multiLines == '23,24,25'
  }

  def 'Allow adding multiple check of same name'() {
    given:
      def extension = new EstiloExtension()
      def closure = {
        checks {
          DescendantToken(override: true, remove: true) {
            id 'stringEqual'
            tokens 'EQUAL,NOT_EQUAL'
            limitedTokens 'STRING_LITERAL'
            maximumNumber 0
            maximumDepth 1
          }
          DescendantToken {
            id 'switchNoDefault'
            tokens 'LITERAL_SWITCH'
            limitedTokens 'LITERAL_DEFAULT'
            maximumNumber 2
            maximumDepth 1
          }
          SingleLineJavadoc(remove: true)
        }
      }
      closure.delegate = extension

    when:
      closure()

    then:
      extension.checkCollection.length == 3

      def check3 = extension.checkCollection.collection.pop()
      def check2 = extension.checkCollection.collection.pop()
      def check1 = extension.checkCollection.collection.pop()

      check1.name == 'DescendantToken'
      check1.id == 'stringEqual'
      check1.tokens == 'EQUAL,NOT_EQUAL'
      check1.limitedTokens == 'STRING_LITERAL'
      check1.maximumNumber == '0'
      check1.maximumDepth == '1'
      check1.customOptions.override == true
      check1.customOptions.remove == true

      check2.name == 'DescendantToken'
      check2.id == 'switchNoDefault'
      check2.tokens == 'LITERAL_SWITCH'
      check2.limitedTokens == 'LITERAL_DEFAULT'
      check2.maximumNumber == '2'
      check2.maximumDepth == '1'
      check2.customOptions.override == false
      check2.customOptions.remove == false

      check3.name == 'SingleLineJavadoc'
  }

  def 'No suppressions and checks are added by default'() {
    when:
      def extension = new EstiloExtension()

    then:
      extension.checkCollection == null
      extension.suppressionCollection == null
      !extension.hasImportControl()
      !extension.hasSuppressions()
      !extension.hasHeader()
  }

  def 'Allow adding suppressions'() {
    given:
      def extension = new EstiloExtension()
      def closure = {
        suppressions {
          suffix 'Test.java', {
            checks 'LineLength'
            lines '23, 34, 45'
            columns([34, 56, 67])
          }
          suffix.not 'Test.java', {
            id 'javadoc'
          }
          prefix 'Draft', {
            checks 'Indentation'
          }
          prefix.not 'Draft', {
            id 'draftHeader'
          }
          contains 'Algorithm', {
            checks 'LineLength'
          }
          contains.not 'Test.java', {
            id 'classJavaDoc'
          }
        }
      }
      closure.delegate = extension

    when:
      closure()

    then:
      extension.suppressionCollection.length == 6
      !extension.hasImportControl()
      extension.hasSuppressions()
      !extension.hasHeader()

      extension.suppressionCollection.collection[0].files == '.*\\QTest.java\\E'
      extension.suppressionCollection.collection[0].checks == 'LineLength'
      extension.suppressionCollection.collection[0].lines == '23, 34, 45'
      extension.suppressionCollection.collection[0].columns == '34,56,67'
      extension.suppressionCollection.collection[0].properties.size() == 4

      extension.suppressionCollection.collection[1].files == '.*(?&lt;!\\QTest.java\\E)$'
      extension.suppressionCollection.collection[1].id == 'javadoc'
      extension.suppressionCollection.collection[1].properties.size() == 2

      extension.suppressionCollection.collection[2].files == '\\QDraft\\E.*'
      extension.suppressionCollection.collection[2].checks == 'Indentation'
      extension.suppressionCollection.collection[2].properties.size() == 2

      extension.suppressionCollection.collection[3].files == '^(?!\\QDraft\\E).*$'
      extension.suppressionCollection.collection[3].id == 'draftHeader'
      extension.suppressionCollection.collection[3].properties.size() == 2

      extension.suppressionCollection.collection[4].files == '.*\\QAlgorithm\\E.*'
      extension.suppressionCollection.collection[4].checks == 'LineLength'
      extension.suppressionCollection.collection[4].properties.size() == 2

      extension.suppressionCollection.collection[5].files == '^((?!\\QTest.java\\E).)*$'
      extension.suppressionCollection.collection[5].id == 'classJavaDoc'
      extension.suppressionCollection.collection[5].properties.size() == 2
  }

  def 'Allow adding import control'() {
    given:
      def extension = new EstiloExtension()
      def closure = {
        importControl 'com', {
          allow pkg: 'java'
          allow pkg: 'javax'
          allow pkg: 'com'
          allow clazz: 'net.anshulverma.gradle.estilo.EstiloTask'

          disallow pkg: 'org'
          disallow clazz: 'org.Unknown'

          subpackage 'com.test.first', {
            allow pkg: 'com'
          }

          subpackage 'com.test.second', {
            allow pkg: 'com'
            allow clazz: 'com.test.Testable'
            disallow pkg: 'com'
            disallow clazz: 'com.unknown.Weird'
          }
        }
      }
      closure.delegate = extension

    when:
      closure()

    then:
      extension.importControlCollection.length == 8
      extension.hasImportControl()
      !extension.hasSuppressions()
      !extension.hasHeader()

      extension.importControlCollection.basePackage == 'com'

      extension.importControlCollection.importControlList[0].type == 'allow'
      extension.importControlCollection.importControlList[0].scope == 'pkg'
      extension.importControlCollection.importControlList[0].value == 'java'

      extension.importControlCollection.importControlList[5].type == 'disallow'
      extension.importControlCollection.importControlList[5].scope == 'class'
      extension.importControlCollection.importControlList[5].value == 'org.Unknown'

      extension.importControlCollection.subPackages.size() == 2

      extension.importControlCollection.subPackages[0].length == 1
      extension.importControlCollection.subPackages[1].length == 4

      extension.importControlCollection.subPackages[1].importControlList[1].type == 'allow'
      extension.importControlCollection.subPackages[1].importControlList[1].scope == 'class'
      extension.importControlCollection.subPackages[1].importControlList[1].value == 'com.test.Testable'
  }

  def 'Allow adding file header check'() {
    given:
      def extension = new EstiloExtension()
      def closure = {
        header regexp: true, multiLines: [22, 23, 24, 25, 27], template: '''^/\\*\\*
^ \\* Copyright © 2015 Anshul Verma. All Rights Reserved.
^ \\*/
'''
      }
      closure.delegate = extension

    when:
      closure()

    then:
      extension.headerCheckOptions.size() == 3
      !extension.hasImportControl()
      !extension.hasSuppressions()
      extension.hasHeader()

      extension.headerCheckOptions.regexp == true
      extension.headerCheckOptions.multiLines == [22, 23, 24, 25, 27]
      extension.headerCheckOptions.template.length() == 70
  }

  def 'allow adding tool version'() {
    given:
      def extension = new EstiloExtension()
      def closure = {
        toolVersion '1.2.3.4'
      }
      closure.delegate = extension

    when:
      closure()

    then:
      extension.checkstyleToolVersion == '1.2.3.4'
  }

  def 'allow ignoring warnings'() {
    given:
      def extension = new EstiloExtension()
      def closure = {
        ignoreWarnings true
      }
      closure.delegate = extension

    when:
      closure()

    then:
      extension.ignoreCheckstyleWarnings
  }
}
