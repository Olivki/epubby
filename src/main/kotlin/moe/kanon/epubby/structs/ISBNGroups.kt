/*
 *   Copyright © 2015 Francisco Sariego Rodríguez
 *
 *   This file is part of ISBNHyphenAppender.
 *
 *   ISBNHyphenAppender is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ISBNHyphenAppender is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with ISBNHyphenAppender.  If not, see <http://www.gnu.org/licenses/>.
 */

package moe.kanon.epubby.structs

/**
 * Represents an ISBN group.
 *
 * Each group is based in the information in `export_rangemessage.xml`.
 *
 * [export_rangemessage.xml](https://www.isbn-international.org/export_rangemessage.xml)
 *
 * @author Francisco Sariego Rodríguez
 *
 * @version 1.0.0-20191209
 */
internal enum class ISBNGroup(
    /**
     * Number of the group.
     */
    val number: Int,
    /**
     * Ranges of valid publisher numbers for the group.
     */
    val validPublisherNumbers: Array<Array<String>>,
    /**
     * Maximum length of a valid publisher number of the group.
     */
    val maximumPublisherNumberLength: Int
) {
    /**
     * Group: 978-0. English language
     */
    _9780(
        9780,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "227"),
            arrayOf<String>("2280", "2289"),
            arrayOf<String>("229", "368"),
            arrayOf<String>("3690", "3699"),
            arrayOf<String>("370", "638"),
            arrayOf<String>("6390", "6397"),
            arrayOf<String>("6398000", "6399999"),
            arrayOf<String>("640", "647"),
            arrayOf<String>("6480000", "6489999"),
            arrayOf<String>("649", "654"),
            arrayOf<String>("6550", "6559"),
            arrayOf<String>("656", "699"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("900000", "949999"),
            arrayOf<String>("9500000", "9999999")
        ),
        7
    ),
    /**
     * Group: 978-1. English language
     */
    _9781(
        9781, arrayOf<Array<String>>(
            arrayOf<String>("000", "009"),
            arrayOf<String>("01", "06"),
            arrayOf<String>("0700", "0999"),
            arrayOf<String>("100", "397"),
            arrayOf<String>("3980", "5499"),
            arrayOf<String>("55000", "66999"),
            arrayOf<String>("6700", "6799"),
            arrayOf<String>("68000", "68599"),
            arrayOf<String>("6860", "7139"),
            arrayOf<String>("714", "716"),
            arrayOf<String>("7170", "7319"),
            arrayOf<String>("7320000", "7399999"),
            arrayOf<String>("74000", "77499"),
            arrayOf<String>("7750000", "7753999"),
            arrayOf<String>("77540", "77639"),
            arrayOf<String>("7764000", "7764999"),
            arrayOf<String>("77650", "77699"),
            arrayOf<String>("7770000", "7776999"),
            arrayOf<String>("77770", "78999"),
            arrayOf<String>("7900", "7999"),
            arrayOf<String>("80000", "86719"),
            arrayOf<String>("8672", "8675"),
            arrayOf<String>("86760", "86979"),
            arrayOf<String>("869800", "915999"),
            arrayOf<String>("9160000", "9165059"),
            arrayOf<String>("916506", "972999"),
            arrayOf<String>("9730", "9877"),
            arrayOf<String>("987800", "998999"),
            arrayOf<String>("9990000", "9999999")
        ), 7
    ),
    /**
     * Group: 978-2. French language
     */
    _9782(
        9782,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "349"),
            arrayOf<String>("35000", "39999"),
            arrayOf<String>("400", "489"),
            arrayOf<String>("490000", "494999"),
            arrayOf<String>("495", "495"),
            arrayOf<String>("4960", "4966"),
            arrayOf<String>("49670", "49699"),
            arrayOf<String>("497", "699"),
            arrayOf<String>("7000", "8399"),
            arrayOf<String>("84000", "89999"),
            arrayOf<String>("900000", "919799"),
            arrayOf<String>("91980", "91980"),
            arrayOf<String>("919810", "919942"),
            arrayOf<String>("9199430", "9199689"),
            arrayOf<String>("919969", "949999"),
            arrayOf<String>("9500000", "9999999")
        ),
        7
    ),
    /**
     * Group: 978-3. German language
     */
    _9783(
        9783,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "02"),
            arrayOf<String>("030", "033"),
            arrayOf<String>("0340", "0369"),
            arrayOf<String>("03700", "03999"),
            arrayOf<String>("04", "19"),
            arrayOf<String>("200", "699"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("900000", "949999"),
            arrayOf<String>("9500000", "9539999"),
            arrayOf<String>("95400", "96999"),
            arrayOf<String>("9700000", "9849999"),
            arrayOf<String>("98500", "99999")
        ),
        7
    ),
    /**
     * Group: 978-4. Japan
     */
    _9784(
        9784,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "699"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("900000", "949999"),
            arrayOf<String>("9500000", "9999999")
        ),
        7
    ),
    /**
     * Group: 978-5. former U.S.S.R
     */
    _9785(
        9785,
        arrayOf<Array<String>>(
            arrayOf<String>("00000", "00499"),
            arrayOf<String>("0050", "0099"),
            arrayOf<String>("01", "19"),
            arrayOf<String>("200", "420"),
            arrayOf<String>("4210", "4299"),
            arrayOf<String>("430", "430"),
            arrayOf<String>("4310", "4399"),
            arrayOf<String>("440", "440"),
            arrayOf<String>("4410", "4499"),
            arrayOf<String>("450", "603"),
            arrayOf<String>("6040000", "6049999"),
            arrayOf<String>("605", "699"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("900000", "909999"),
            arrayOf<String>("91000", "91999"),
            arrayOf<String>("9200", "9299"),
            arrayOf<String>("93000", "94999"),
            arrayOf<String>("9500000", "9500999"),
            arrayOf<String>("9501", "9799"),
            arrayOf<String>("98000", "98999"),
            arrayOf<String>("9900000", "9909999"),
            arrayOf<String>("9910", "9999")
        ),
        7
    ),
    /**
     * Group: 978-600. Iran
     */
    _978600(
        978600,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "09"),
            arrayOf<String>("100", "499"),
            arrayOf<String>("5000", "8999"),
            arrayOf<String>("90000", "98679"),
            arrayOf<String>("9868", "9929"),
            arrayOf<String>("993", "995"),
            arrayOf<String>("99600", "99999")
        ),
        5
    ),
    /**
     * Group: 978-601. Kazakhstan
     */
    _978601(
        978601,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "699"),
            arrayOf<String>("7000", "7999"),
            arrayOf<String>("80000", "84999"),
            arrayOf<String>("85", "99")
        ),
        5
    ),
    /**
     * Group: 978-602. Indonesia
     */
    _978602(
        978602,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "06"),
            arrayOf<String>("0700", "1399"),
            arrayOf<String>("14000", "14999"),
            arrayOf<String>("1500", "1699"),
            arrayOf<String>("17000", "19999"),
            arrayOf<String>("200", "499"),
            arrayOf<String>("50000", "53999"),
            arrayOf<String>("5400", "5999"),
            arrayOf<String>("60000", "61999"),
            arrayOf<String>("6200", "6999"),
            arrayOf<String>("70000", "74999"),
            arrayOf<String>("7500", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-603. Saudi Arabia
     */
    _978603(
        978603,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "04"),
            arrayOf<String>("05", "49"),
            arrayOf<String>("500", "799"),
            arrayOf<String>("8000", "8999"),
            arrayOf<String>("90000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-604. Vietnam
     */
    _978604(
        978604,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "89"),
            arrayOf<String>("900", "979"),
            arrayOf<String>("9800", "9999")
        ),
        4
    ),
    /**
     * Group: 978-605. Turkey
     */
    _978605(
        978605,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "02"),
            arrayOf<String>("030", "039"),
            arrayOf<String>("04", "09"),
            arrayOf<String>("100", "199"),
            arrayOf<String>("2000", "2399"),
            arrayOf<String>("240", "399"),
            arrayOf<String>("4000", "5999"),
            arrayOf<String>("60000", "74999"),
            arrayOf<String>("7500", "7999"),
            arrayOf<String>("80000", "89999"),
            arrayOf<String>("9000", "9999")
        ),
        5
    ),
    /**
     * Group: 978-606. Romania
     */
    _978606(
        978606,
        arrayOf<Array<String>>(
            arrayOf<String>("000", "089"),
            arrayOf<String>("09", "49"),
            arrayOf<String>("500", "799"),
            arrayOf<String>("8000", "9099"),
            arrayOf<String>("910", "919"),
            arrayOf<String>("92000", "96499"),
            arrayOf<String>("9650", "9749"),
            arrayOf<String>("975", "999")
        ),
        5
    ),
    /**
     * Group: 978-607. Mexico
     */
    _978607(
        978607,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "749"),
            arrayOf<String>("7500", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-608. Macedonia
     */
    _978608(
        978608,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "19"),
            arrayOf<String>("200", "449"),
            arrayOf<String>("4500", "6499"),
            arrayOf<String>("65000", "69999"),
            arrayOf<String>("7", "9")
        ),
        5
    ),
    /**
     * Group: 978-609. Lithuania
     */
    _978609(
        978609,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "799"),
            arrayOf<String>("8000", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-611. Thailand
     */
    _978611(978611, arrayOf<Array<String>>(arrayOf<String>("0000000", "9999999")), 0),
    /**
     * Group: 978-612. Peru
     */
    _978612(
        978612,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "29"),
            arrayOf<String>("300", "399"),
            arrayOf<String>("4000", "4499"),
            arrayOf<String>("45000", "49999"),
            arrayOf<String>("50", "99")
        ),
        5
    ),
    /**
     * Group: 978-613. Mauritius
     */
    _978613(978613, arrayOf<Array<String>>(arrayOf<String>("0", "9")), 1),
    /**
     * Group: 978-614. Lebanon
     */
    _978614(
        978614,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "799"),
            arrayOf<String>("8000", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-615. Hungary
     */
    _978615(
        978615,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "09"),
            arrayOf<String>("100", "499"),
            arrayOf<String>("5000", "7999"),
            arrayOf<String>("80000", "89999"),
            arrayOf<String>("9000000", "9999999")
        ),
        5
    ),
    /**
     * Group: 978-616. Thailand
     */
    _978616(
        978616,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "699"),
            arrayOf<String>("7000", "8999"),
            arrayOf<String>("90000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-617. Ukraine
     */
    _978617(
        978617,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "49"),
            arrayOf<String>("500", "699"),
            arrayOf<String>("7000", "8999"),
            arrayOf<String>("90000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-618. Greece
     */
    _978618(
        978618,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "499"),
            arrayOf<String>("5000", "7999"),
            arrayOf<String>("80000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-619. Bulgaria
     */
    _978619(
        978619,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "14"),
            arrayOf<String>("150", "699"),
            arrayOf<String>("7000", "8999"),
            arrayOf<String>("90000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-620. Mauritius
     */
    _978620(978620, arrayOf<Array<String>>(arrayOf<String>("0", "9")), 1),
    /**
     * Group: 978-621. Philippines
     */
    _978621(
        978621,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "29"),
            arrayOf<String>("3000000", "3999999"),
            arrayOf<String>("400", "599"),
            arrayOf<String>("6000000", "7999999"),
            arrayOf<String>("8000", "8999"),
            arrayOf<String>("9000000", "9499999"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-622. Iran
     */
    _978622(
        978622,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "08"),
            arrayOf<String>("0900000", "1999999"),
            arrayOf<String>("200", "299"),
            arrayOf<String>("3000000", "5999999"),
            arrayOf<String>("6000", "7499"),
            arrayOf<String>("7500000", "9499999"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-623. Indonesia
     */
    _978623(
        978623,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "09"),
            arrayOf<String>("1000000", "1999999"),
            arrayOf<String>("200", "299"),
            arrayOf<String>("3000000", "6999999"),
            arrayOf<String>("7000", "7999"),
            arrayOf<String>("8000000", "8999999"),
            arrayOf<String>("90000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-624. Sri Lanka
     */
    _978624(
        978624,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "04"),
            arrayOf<String>("0500000", "1999999"),
            arrayOf<String>("200", "249"),
            arrayOf<String>("2500000", "4999999"),
            arrayOf<String>("5000", "5999"),
            arrayOf<String>("6000000", "9499999"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-625. Turkey
     */
    _978625(
        978625,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "00"),
            arrayOf<String>("0100000", "3999999"),
            arrayOf<String>("400", "449"),
            arrayOf<String>("4500000", "6999999"),
            arrayOf<String>("7000", "7999"),
            arrayOf<String>("8000000", "9999999")
        ),
        4
    ),
    /**
     * Group: 978-65. Brazil
     */
    _97865(
        97865,
        arrayOf<Array<String>>(
            arrayOf<String>("0000000", "2999999"),
            arrayOf<String>("300", "302"),
            arrayOf<String>("3030000", "4999999"),
            arrayOf<String>("5000", "5104"),
            arrayOf<String>("5105000", "7999999"),
            arrayOf<String>("80000", "81699"),
            arrayOf<String>("8170000", "8999999"),
            arrayOf<String>("900000", "902199"),
            arrayOf<String>("9022000", "9999999")
        ),
        6
    ),
    /**
     * Group: 978-7. China, People's Republic
     */
    _9787(
        9787,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "09"),
            arrayOf<String>("100", "499"),
            arrayOf<String>("5000", "7999"),
            arrayOf<String>("80000", "89999"),
            arrayOf<String>("900000", "999999")
        ),
        6
    ),
    /**
     * Group: 978-80. former Czechoslovakia
     */
    _97880(
        97880,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "699"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("900000", "998999"),
            arrayOf<String>("99900", "99999")
        ),
        6
    ),
    /**
     * Group: 978-81. India
     */
    _97881(
        97881,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "699"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("900000", "999999")
        ),
        6
    ),
    /**
     * Group: 978-82. Norway
     */
    _97882(
        97882,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "689"),
            arrayOf<String>("690000", "699999"),
            arrayOf<String>("7000", "8999"),
            arrayOf<String>("90000", "98999"),
            arrayOf<String>("990000", "999999")
        ),
        6
    ),
    /**
     * Group: 978-83. Poland
     */
    _97883(
        97883,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "599"),
            arrayOf<String>("60000", "69999"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("900000", "999999")
        ),
        6
    ),
    /**
     * Group: 978-84. Spain
     */
    _97884(
        97884,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "11"),
            arrayOf<String>("120000", "129999"),
            arrayOf<String>("1300", "1399"),
            arrayOf<String>("140", "149"),
            arrayOf<String>("15000", "19999"),
            arrayOf<String>("200", "699"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("9000", "9199"),
            arrayOf<String>("920000", "923999"),
            arrayOf<String>("92400", "92999"),
            arrayOf<String>("930000", "949999"),
            arrayOf<String>("95000", "96999"),
            arrayOf<String>("9700", "9999")
        ),
        6
    ),
    /**
     * Group: 978-85. Brazil
     */
    _97885(
        97885, arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "454"),
            arrayOf<String>("455000", "455299"),
            arrayOf<String>("45530", "45599"),
            arrayOf<String>("456", "528"),
            arrayOf<String>("52900", "53199"),
            arrayOf<String>("5320", "5339"),
            arrayOf<String>("534", "539"),
            arrayOf<String>("54000", "54029"),
            arrayOf<String>("54030", "54039"),
            arrayOf<String>("540400", "540499"),
            arrayOf<String>("54050", "54089"),
            arrayOf<String>("540900", "540999"),
            arrayOf<String>("54100", "54399"),
            arrayOf<String>("5440", "5479"),
            arrayOf<String>("54800", "54999"),
            arrayOf<String>("5500", "5999"),
            arrayOf<String>("60000", "69999"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("900000", "924999"),
            arrayOf<String>("92500", "94499"),
            arrayOf<String>("9450", "9599"),
            arrayOf<String>("96", "97"),
            arrayOf<String>("98000", "99999")
        ), 6
    ),
    /**
     * Group: 978-86. former Yugoslavia
     */
    _97886(
        97886,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "29"),
            arrayOf<String>("300", "599"),
            arrayOf<String>("6000", "7999"),
            arrayOf<String>("80000", "89999"),
            arrayOf<String>("900000", "999999")
        ),
        6
    ),
    /**
     * Group: 978-87. Denmark
     */
    _97887(
        97887,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "29"),
            arrayOf<String>("3000000", "3999999"),
            arrayOf<String>("400", "649"),
            arrayOf<String>("6500000", "6999999"),
            arrayOf<String>("7000", "7999"),
            arrayOf<String>("8000000", "8499999"),
            arrayOf<String>("85000", "94999"),
            arrayOf<String>("9500000", "9699999"),
            arrayOf<String>("970000", "999999")
        ),
        6
    ),
    /**
     * Group: 978-88. Italy
     */
    _97888(
        97888,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "311"),
            arrayOf<String>("31200", "31499"),
            arrayOf<String>("315", "318"),
            arrayOf<String>("31900", "32299"),
            arrayOf<String>("323", "326"),
            arrayOf<String>("3270", "3389"),
            arrayOf<String>("339", "360"),
            arrayOf<String>("3610", "3629"),
            arrayOf<String>("363", "548"),
            arrayOf<String>("5490", "5549"),
            arrayOf<String>("555", "599"),
            arrayOf<String>("6000", "8499"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("900000", "909999"),
            arrayOf<String>("910", "926"),
            arrayOf<String>("9270", "9399"),
            arrayOf<String>("940000", "947999"),
            arrayOf<String>("94800", "99999")
        ),
        6
    ),
    /**
     * Group: 978-89. Korea, Republic
     */
    _97889(
        97889,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "24"),
            arrayOf<String>("250", "549"),
            arrayOf<String>("5500", "8499"),
            arrayOf<String>("85000", "94999"),
            arrayOf<String>("950000", "969999"),
            arrayOf<String>("97000", "98999"),
            arrayOf<String>("990", "999")
        ),
        6
    ),
    /**
     * Group: 978-90. Netherlands
     */
    _97890(
        97890,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "499"),
            arrayOf<String>("5000", "6999"),
            arrayOf<String>("70000", "79999"),
            arrayOf<String>("800000", "849999"),
            arrayOf<String>("8500", "8999"),
            arrayOf<String>("90", "90"),
            arrayOf<String>("9100000", "9399999"),
            arrayOf<String>("94", "94"),
            arrayOf<String>("9500000", "9999999")
        ),
        6
    ),
    /**
     * Group: 978-91. Sweden
     */
    _97891(
        97891,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "49"),
            arrayOf<String>("500", "649"),
            arrayOf<String>("6500000", "6999999"),
            arrayOf<String>("7000", "8199"),
            arrayOf<String>("8200000", "8499999"),
            arrayOf<String>("85000", "94999"),
            arrayOf<String>("9500000", "9699999"),
            arrayOf<String>("970000", "999999")
        ),
        6
    ),
    /**
     * Group: 978-92. International NGO Publishers and EU Organizations
     */
    _97892(
        97892,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "5"),
            arrayOf<String>("60", "79"),
            arrayOf<String>("800", "899"),
            arrayOf<String>("9000", "9499"),
            arrayOf<String>("95000", "98999"),
            arrayOf<String>("990000", "999999")
        ),
        6
    ),
    /**
     * Group: 978-93. India
     */
    _97893(
        97893,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "09"),
            arrayOf<String>("100", "499"),
            arrayOf<String>("5000", "7999"),
            arrayOf<String>("80000", "94999"),
            arrayOf<String>("950000", "999999")
        ),
        6
    ),
    /**
     * Group: 978-94. Netherlands
     */
    _97894(
        97894,
        arrayOf<Array<String>>(
            arrayOf<String>("000", "599"),
            arrayOf<String>("6000", "8999"),
            arrayOf<String>("90000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-950. Argentina
     */
    _978950(
        978950,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "49"),
            arrayOf<String>("500", "899"),
            arrayOf<String>("9000", "9899"),
            arrayOf<String>("99000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-951. Finland
     */
    _978951(
        978951,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "54"),
            arrayOf<String>("550", "889"),
            arrayOf<String>("8900", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-952. Finland
     */
    _978952(
        978952,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "499"),
            arrayOf<String>("5000", "5999"),
            arrayOf<String>("60", "65"),
            arrayOf<String>("6600", "6699"),
            arrayOf<String>("67000", "69999"),
            arrayOf<String>("7000", "7999"),
            arrayOf<String>("80", "94"),
            arrayOf<String>("9500", "9899"),
            arrayOf<String>("99000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-953. Croatia
     */
    _978953(
        978953,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "14"),
            arrayOf<String>("150", "479"),
            arrayOf<String>("48000", "49999"),
            arrayOf<String>("500", "500"),
            arrayOf<String>("50100", "50999"),
            arrayOf<String>("51", "54"),
            arrayOf<String>("55000", "59999"),
            arrayOf<String>("6000", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-954. Bulgaria
     */
    _978954(
        978954,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "28"),
            arrayOf<String>("2900", "2999"),
            arrayOf<String>("300", "799"),
            arrayOf<String>("8000", "8999"),
            arrayOf<String>("90000", "92999"),
            arrayOf<String>("9300", "9999")
        ),
        5
    ),
    /**
     * Group: 978-955. Sri Lanka
     */
    _978955(
        978955,
        arrayOf<Array<String>>(
            arrayOf<String>("0000", "1999"),
            arrayOf<String>("20", "33"),
            arrayOf<String>("3400", "3549"),
            arrayOf<String>("35500", "35999"),
            arrayOf<String>("3600", "3799"),
            arrayOf<String>("38000", "38999"),
            arrayOf<String>("3900", "4099"),
            arrayOf<String>("41000", "44999"),
            arrayOf<String>("4500", "4999"),
            arrayOf<String>("50000", "54999"),
            arrayOf<String>("550", "710"),
            arrayOf<String>("71100", "71499"),
            arrayOf<String>("7150", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-956. Chile
     */
    _978956(
        978956,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "08"),
            arrayOf<String>("09000", "09999"),
            arrayOf<String>("10", "19"),
            arrayOf<String>("200", "599"),
            arrayOf<String>("6000", "6999"),
            arrayOf<String>("7000", "9999")
        ),
        5
    ),
    /**
     * Group: 978-957. Taiwan
     */
    _978957(
        978957,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "02"),
            arrayOf<String>("0300", "0499"),
            arrayOf<String>("05", "19"),
            arrayOf<String>("2000", "2099"),
            arrayOf<String>("21", "27"),
            arrayOf<String>("28000", "30999"),
            arrayOf<String>("31", "43"),
            arrayOf<String>("440", "819"),
            arrayOf<String>("8200", "9699"),
            arrayOf<String>("97000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-958. Colombia
     */
    _978958(
        978958,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "50"),
            arrayOf<String>("5100", "5199"),
            arrayOf<String>("52000", "53999"),
            arrayOf<String>("5400", "5599"),
            arrayOf<String>("56000", "59999"),
            arrayOf<String>("600", "799"),
            arrayOf<String>("8000", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-959. Cuba
     */
    _978959(
        978959,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "699"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-960. Greece
     */
    _978960(
        978960,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "659"),
            arrayOf<String>("6600", "6899"),
            arrayOf<String>("690", "699"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "92999"),
            arrayOf<String>("93", "93"),
            arrayOf<String>("9400", "9799"),
            arrayOf<String>("98000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-961. Slovenia
     */
    _978961(
        978961,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "599"),
            arrayOf<String>("6000", "8999"),
            arrayOf<String>("90000", "94999"),
            arrayOf<String>("9500000", "9999999")
        ),
        5
    ),
    /**
     * Group: 978-962. Hong Kong, China
     */
    _978962(
        978962,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "699"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "86999"),
            arrayOf<String>("8700", "8999"),
            arrayOf<String>("900", "999")
        ),
        5
    ),
    /**
     * Group: 978-963. Hungary
     */
    _978963(
        978963,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "699"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("9000", "9999")
        ),
        5
    ),
    /**
     * Group: 978-964. Iran
     */
    _978964(
        978964,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "14"),
            arrayOf<String>("150", "249"),
            arrayOf<String>("2500", "2999"),
            arrayOf<String>("300", "549"),
            arrayOf<String>("5500", "8999"),
            arrayOf<String>("90000", "96999"),
            arrayOf<String>("970", "989"),
            arrayOf<String>("9900", "9999")
        ),
        5
    ),
    /**
     * Group: 978-965. Israel
     */
    _978965(
        978965,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "599"),
            arrayOf<String>("6000000", "6999999"),
            arrayOf<String>("7000", "7999"),
            arrayOf<String>("8000000", "8999999"),
            arrayOf<String>("90000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-966. Ukraine
     */
    _978966(
        978966,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "12"),
            arrayOf<String>("130", "139"),
            arrayOf<String>("14", "14"),
            arrayOf<String>("1500", "1699"),
            arrayOf<String>("170", "199"),
            arrayOf<String>("2000", "2789"),
            arrayOf<String>("279", "289"),
            arrayOf<String>("2900", "2999"),
            arrayOf<String>("300", "699"),
            arrayOf<String>("7000", "8999"),
            arrayOf<String>("90000", "90999"),
            arrayOf<String>("910", "949"),
            arrayOf<String>("95000", "97999"),
            arrayOf<String>("980", "999")
        ),
        5
    ),
    /**
     * Group: 978-967. Malaysia
     */
    _978967(
        978967,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "00"),
            arrayOf<String>("0100", "0999"),
            arrayOf<String>("10000", "19999"),
            arrayOf<String>("2000", "2499"),
            arrayOf<String>("2500000", "2999999"),
            arrayOf<String>("300", "499"),
            arrayOf<String>("5000", "5999"),
            arrayOf<String>("60", "89"),
            arrayOf<String>("900", "989"),
            arrayOf<String>("9900", "9989"),
            arrayOf<String>("99900", "99999")
        ),
        5
    ),
    /**
     * Group: 978-968. Mexico
     */
    _978968(
        978968,
        arrayOf<Array<String>>(
            arrayOf<String>("01", "39"),
            arrayOf<String>("400", "499"),
            arrayOf<String>("5000", "7999"),
            arrayOf<String>("800", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-969. Pakistan
     */
    _978969(
        978969,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "22"),
            arrayOf<String>("23000", "23999"),
            arrayOf<String>("24", "39"),
            arrayOf<String>("400", "749"),
            arrayOf<String>("7500", "9999")
        ),
        5
    ),
    /**
     * Group: 978-970. Mexico
     */
    _978970(
        978970,
        arrayOf<Array<String>>(
            arrayOf<String>("01", "59"),
            arrayOf<String>("600", "899"),
            arrayOf<String>("9000", "9099"),
            arrayOf<String>("91000", "96999"),
            arrayOf<String>("9700", "9999")
        ),
        5
    ),
    /**
     * Group: 978-971. Philippines
     */
    _978971(
        978971,
        arrayOf<Array<String>>(
            arrayOf<String>("000", "015"),
            arrayOf<String>("0160", "0199"),
            arrayOf<String>("02", "02"),
            arrayOf<String>("0300", "0599"),
            arrayOf<String>("06", "49"),
            arrayOf<String>("500", "849"),
            arrayOf<String>("8500", "9099"),
            arrayOf<String>("91000", "95999"),
            arrayOf<String>("9600", "9699"),
            arrayOf<String>("97", "98"),
            arrayOf<String>("9900", "9999")
        ),
        5
    ),
    /**
     * Group: 978-972. Portugal
     */
    _978972(
        978972,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "54"),
            arrayOf<String>("550", "799"),
            arrayOf<String>("8000", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-973. Romania
     */
    _978973(
        978973,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("100", "169"),
            arrayOf<String>("1700", "1999"),
            arrayOf<String>("20", "54"),
            arrayOf<String>("550", "759"),
            arrayOf<String>("7600", "8499"),
            arrayOf<String>("85000", "88999"),
            arrayOf<String>("8900", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-974. Thailand
     */
    _978974(
        978974,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "699"),
            arrayOf<String>("7000", "8499"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("90000", "94999"),
            arrayOf<String>("9500", "9999")
        ),
        5
    ),
    /**
     * Group: 978-975. Turkey
     */
    _978975(
        978975,
        arrayOf<Array<String>>(
            arrayOf<String>("00000", "01999"),
            arrayOf<String>("02", "23"),
            arrayOf<String>("2400", "2499"),
            arrayOf<String>("250", "599"),
            arrayOf<String>("6000", "9199"),
            arrayOf<String>("92000", "98999"),
            arrayOf<String>("990", "999")
        ),
        5
    ),
    /**
     * Group: 978-976. Caribbean Community
     */
    _978976(
        978976,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "3"),
            arrayOf<String>("40", "59"),
            arrayOf<String>("600", "799"),
            arrayOf<String>("8000", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-977. Egypt
     */
    _978977(
        978977,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "499"),
            arrayOf<String>("5000", "6999"),
            arrayOf<String>("700", "849"),
            arrayOf<String>("85000", "89999"),
            arrayOf<String>("90", "98"),
            arrayOf<String>("990", "999")
        ),
        5
    ),
    /**
     * Group: 978-978. Nigeria
     */
    _978978(
        978978,
        arrayOf<Array<String>>(
            arrayOf<String>("000", "199"),
            arrayOf<String>("2000", "2999"),
            arrayOf<String>("30000", "79999"),
            arrayOf<String>("8000", "8999"),
            arrayOf<String>("900", "999")
        ),
        5
    ),
    /**
     * Group: 978-979. Indonesia
     */
    _978979(
        978979,
        arrayOf<Array<String>>(
            arrayOf<String>("000", "099"),
            arrayOf<String>("1000", "1499"),
            arrayOf<String>("15000", "19999"),
            arrayOf<String>("20", "29"),
            arrayOf<String>("3000", "3999"),
            arrayOf<String>("400", "799"),
            arrayOf<String>("8000", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-980. Venezuela
     */
    _978980(
        978980,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "599"),
            arrayOf<String>("6000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-981. Singapore
     */
    _978981(
        978981,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "16"),
            arrayOf<String>("17000", "17999"),
            arrayOf<String>("18", "19"),
            arrayOf<String>("200", "299"),
            arrayOf<String>("3000", "3099"),
            arrayOf<String>("310", "399"),
            arrayOf<String>("4000", "9999")
        ),
        5
    ),
    /**
     * Group: 978-982. South Pacific
     */
    _978982(
        978982,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "09"),
            arrayOf<String>("100", "699"),
            arrayOf<String>("70", "89"),
            arrayOf<String>("9000", "9799"),
            arrayOf<String>("98000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-983. Malaysia
     */
    _978983(
        978983,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "01"),
            arrayOf<String>("020", "199"),
            arrayOf<String>("2000", "3999"),
            arrayOf<String>("40000", "44999"),
            arrayOf<String>("45", "49"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "899"),
            arrayOf<String>("9000", "9899"),
            arrayOf<String>("99000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-984. Bangladesh
     */
    _978984(
        978984,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "799"),
            arrayOf<String>("8000", "8999"),
            arrayOf<String>("90000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-985. Belarus
     */
    _978985(
        978985,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "599"),
            arrayOf<String>("6000", "8799"),
            arrayOf<String>("880", "899"),
            arrayOf<String>("90000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-986. Taiwan
     */
    _978986(
        978986,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "11"),
            arrayOf<String>("120", "539"),
            arrayOf<String>("5400", "7999"),
            arrayOf<String>("80000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-987. Argentina
     */
    _978987(
        978987,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "09"),
            arrayOf<String>("1000", "1999"),
            arrayOf<String>("20000", "29999"),
            arrayOf<String>("30", "35"),
            arrayOf<String>("3600", "3999"),
            arrayOf<String>("4000", "4199"),
            arrayOf<String>("42", "43"),
            arrayOf<String>("4400", "4499"),
            arrayOf<String>("45000", "48999"),
            arrayOf<String>("4900", "4999"),
            arrayOf<String>("500", "829"),
            arrayOf<String>("8300", "8499"),
            arrayOf<String>("85", "89"),
            arrayOf<String>("9000", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-988. Hong Kong, China
     */
    _978988(
        978988,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "11"),
            arrayOf<String>("12000", "19999"),
            arrayOf<String>("200", "739"),
            arrayOf<String>("74000", "76999"),
            arrayOf<String>("77000", "79999"),
            arrayOf<String>("8000", "9699"),
            arrayOf<String>("97000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-989. Portugal
     */
    _978989(
        978989,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "53"),
            arrayOf<String>("54000", "54999"),
            arrayOf<String>("550", "799"),
            arrayOf<String>("8000", "9499"),
            arrayOf<String>("95000", "99999")
        ),
        5
    ),
    /**
     * Group: 978-9917. Bolivia
     */
    _9789917(
        9789917,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("1000000", "2999999"),
            arrayOf<String>("30", "34"),
            arrayOf<String>("3500000", "5999999"),
            arrayOf<String>("600", "699"),
            arrayOf<String>("7000000", "9799999"),
            arrayOf<String>("9800", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9918. Malta
     */
    _9789918(
        9789918,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("1000000", "1999999"),
            arrayOf<String>("20", "29"),
            arrayOf<String>("3000000", "5999999"),
            arrayOf<String>("600", "799"),
            arrayOf<String>("8000000", "9499999"),
            arrayOf<String>("9500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9919. Mongolia
     */
    _9789919(
        9789919,
        arrayOf<Array<String>>(
            arrayOf<String>("0000000", "1999999"),
            arrayOf<String>("20", "27"),
            arrayOf<String>("2800000", "4999999"),
            arrayOf<String>("500", "599"),
            arrayOf<String>("6000000", "9499999"),
            arrayOf<String>("9500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9920. Morocco
     */
    _9789920(
        9789920,
        arrayOf<Array<String>>(
            arrayOf<String>("0000000", "3499999"),
            arrayOf<String>("35", "39"),
            arrayOf<String>("4000000", "5999999"),
            arrayOf<String>("600", "799"),
            arrayOf<String>("8000000", "9299999"),
            arrayOf<String>("9300", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9921. Kuwait
     */
    _9789921(
        9789921,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("1000000", "2999999"),
            arrayOf<String>("30", "39"),
            arrayOf<String>("4000000", "6999999"),
            arrayOf<String>("700", "899"),
            arrayOf<String>("9000000", "9699999"),
            arrayOf<String>("9700", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9922. Iraq
     */
    _9789922(
        9789922,
        arrayOf<Array<String>>(
            arrayOf<String>("0000000", "1999999"),
            arrayOf<String>("20", "29"),
            arrayOf<String>("3000000", "5999999"),
            arrayOf<String>("600", "799"),
            arrayOf<String>("8000000", "8999999"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9923. Jordan
     */
    _9789923(
        9789923,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "49"),
            arrayOf<String>("5000000", "6999999"),
            arrayOf<String>("700", "899"),
            arrayOf<String>("9000000", "9699999"),
            arrayOf<String>("9700", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9924. Cambodia
     */
    _9789924(
        9789924,
        arrayOf<Array<String>>(
            arrayOf<String>("0000000", "2999999"),
            arrayOf<String>("30", "39"),
            arrayOf<String>("4000000", "4999999"),
            arrayOf<String>("500", "649"),
            arrayOf<String>("6500000", "8999999"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9925. Cyprus
     */
    _9789925(
        9789925,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "54"),
            arrayOf<String>("550", "734"),
            arrayOf<String>("7350", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9926. Bosnia and Herzegovina
     */
    _9789926(
        9789926,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "39"),
            arrayOf<String>("400", "799"),
            arrayOf<String>("8000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9927. Qatar
     */
    _9789927(
        9789927,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "09"),
            arrayOf<String>("100", "399"),
            arrayOf<String>("4000", "4999"),
            arrayOf<String>("5000000", "9999999")
        ),
        4
    ),
    /**
     * Group: 978-9928. Albania
     */
    _9789928(
        9789928,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "09"),
            arrayOf<String>("100", "399"),
            arrayOf<String>("4000", "4999"),
            arrayOf<String>("5000000", "9999999")
        ),
        4
    ),
    /**
     * Group: 978-9929. Guatemala
     */
    _9789929(
        9789929,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "3"),
            arrayOf<String>("40", "54"),
            arrayOf<String>("550", "799"),
            arrayOf<String>("8000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9930. Costa Rica
     */
    _9789930(
        9789930,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "49"),
            arrayOf<String>("500", "939"),
            arrayOf<String>("9400", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9931. Algeria
     */
    _9789931(
        9789931,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "29"),
            arrayOf<String>("300", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9932. Lao People's Democratic Republic
     */
    _9789932(
        9789932,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "849"),
            arrayOf<String>("8500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9933. Syria
     */
    _9789933(
        9789933,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "39"),
            arrayOf<String>("400", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9934. Latvia
     */
    _9789934(
        9789934,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "49"),
            arrayOf<String>("500", "799"),
            arrayOf<String>("8000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9935. Iceland
     */
    _9789935(
        9789935,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "39"),
            arrayOf<String>("400", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9936. Afghanistan
     */
    _9789936(
        9789936,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "39"),
            arrayOf<String>("400", "799"),
            arrayOf<String>("8000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9937. Nepal
     */
    _9789937(
        9789937,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "49"),
            arrayOf<String>("500", "799"),
            arrayOf<String>("8000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9938. Tunisia
     */
    _9789938(
        9789938,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "79"),
            arrayOf<String>("800", "949"),
            arrayOf<String>("9500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9939. Armenia
     */
    _9789939(
        9789939,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "899"),
            arrayOf<String>("9000", "9799"),
            arrayOf<String>("98", "99")
        ),
        4
    ),
    /**
     * Group: 978-9940. Montenegro
     */
    _9789940(
        9789940,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "49"),
            arrayOf<String>("500", "839"),
            arrayOf<String>("84", "86"),
            arrayOf<String>("8700", "8999"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9941. Georgia
     */
    _9789941(
        9789941,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "39"),
            arrayOf<String>("400", "799"),
            arrayOf<String>("8", "8"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9942. Ecuador
     */
    _9789942(
        9789942,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "74"),
            arrayOf<String>("750", "849"),
            arrayOf<String>("8500", "8999"),
            arrayOf<String>("900", "984"),
            arrayOf<String>("9850", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9943. Uzbekistan
     */
    _9789943(
        9789943,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "29"),
            arrayOf<String>("300", "399"),
            arrayOf<String>("4000", "9749"),
            arrayOf<String>("975", "999")
        ),
        4
    ),
    /**
     * Group: 978-9944. Turkey
     */
    _9789944(
        9789944,
        arrayOf<Array<String>>(
            arrayOf<String>("0000", "0999"),
            arrayOf<String>("100", "499"),
            arrayOf<String>("5000", "5999"),
            arrayOf<String>("60", "69"),
            arrayOf<String>("700", "799"),
            arrayOf<String>("80", "89"),
            arrayOf<String>("900", "999")
        ),
        4
    ),
    /**
     * Group: 978-9945. Dominican Republic
     */
    _9789945(
        9789945,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "00"),
            arrayOf<String>("010", "079"),
            arrayOf<String>("08", "39"),
            arrayOf<String>("400", "569"),
            arrayOf<String>("57", "57"),
            arrayOf<String>("580", "849"),
            arrayOf<String>("8500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9946. Korea, P.D.R.
     */
    _9789946(
        9789946,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "39"),
            arrayOf<String>("400", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9947. Algeria
     */
    _9789947(
        9789947,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-9948. United Arab Emirates
     */
    _9789948(
        9789948,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "849"),
            arrayOf<String>("8500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9949. Estonia
     */
    _9789949(
        9789949,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "08"),
            arrayOf<String>("090", "099"),
            arrayOf<String>("10", "39"),
            arrayOf<String>("400", "699"),
            arrayOf<String>("70", "71"),
            arrayOf<String>("7200", "7499"),
            arrayOf<String>("75", "89"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9950. Palestine
     */
    _9789950(
        9789950,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "29"),
            arrayOf<String>("300", "849"),
            arrayOf<String>("8500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9951. Kosova
     */
    _9789951(
        9789951,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "849"),
            arrayOf<String>("8500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9952. Azerbaijan
     */
    _9789952(
        9789952,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "39"),
            arrayOf<String>("400", "799"),
            arrayOf<String>("8000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9953. Lebanon
     */
    _9789953(
        9789953,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "39"),
            arrayOf<String>("400", "599"),
            arrayOf<String>("60", "89"),
            arrayOf<String>("9000", "9299"),
            arrayOf<String>("93", "96"),
            arrayOf<String>("970", "999")
        ),
        4
    ),
    /**
     * Group: 978-9954. Morocco
     */
    _9789954(
        9789954,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "39"),
            arrayOf<String>("400", "799"),
            arrayOf<String>("8000", "9899"),
            arrayOf<String>("99", "99")
        ),
        4
    ),
    /**
     * Group: 978-9955. Lithuania
     */
    _9789955(
        9789955,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "929"),
            arrayOf<String>("9300", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9956. Cameroon
     */
    _9789956(
        9789956,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "39"),
            arrayOf<String>("400", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9957. Jordan
     */
    _9789957(
        9789957,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "649"),
            arrayOf<String>("65", "67"),
            arrayOf<String>("680", "699"),
            arrayOf<String>("70", "84"),
            arrayOf<String>("8500", "8799"),
            arrayOf<String>("88", "99")
        ),
        4
    ),
    /**
     * Group: 978-9958. Bosnia and Herzegovina
     */
    _9789958(
        9789958,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "01"),
            arrayOf<String>("020", "029"),
            arrayOf<String>("0300", "0399"),
            arrayOf<String>("040", "089"),
            arrayOf<String>("0900", "0999"),
            arrayOf<String>("10", "18"),
            arrayOf<String>("1900", "1999"),
            arrayOf<String>("20", "49"),
            arrayOf<String>("500", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9959. Libya
     */
    _9789959(
        9789959,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "79"),
            arrayOf<String>("800", "949"),
            arrayOf<String>("9500", "9699"),
            arrayOf<String>("970", "979"),
            arrayOf<String>("98", "99")
        ),
        4
    ),
    /**
     * Group: 978-9960. Saudi Arabia
     */
    _9789960(
        9789960,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "59"),
            arrayOf<String>("600", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9961. Algeria
     */
    _9789961(
        9789961,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "69"),
            arrayOf<String>("700", "949"),
            arrayOf<String>("9500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9962. Panama
     */
    _9789962(
        9789962,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "54"),
            arrayOf<String>("5500", "5599"),
            arrayOf<String>("56", "59"),
            arrayOf<String>("600", "849"),
            arrayOf<String>("8500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9963. Cyprus
     */
    _9789963(
        9789963,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("2000", "2499"),
            arrayOf<String>("250", "279"),
            arrayOf<String>("2800", "2999"),
            arrayOf<String>("30", "54"),
            arrayOf<String>("550", "734"),
            arrayOf<String>("7350", "7499"),
            arrayOf<String>("7500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9964. Ghana
     */
    _9789964(
        9789964,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "6"),
            arrayOf<String>("70", "94"),
            arrayOf<String>("950", "999")
        ),
        3
    ),
    /**
     * Group: 978-9965. Kazakhstan
     */
    _9789965(
        9789965,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9966. Kenya
     */
    _9789966(
        9789966,
        arrayOf<Array<String>>(
            arrayOf<String>("000", "139"),
            arrayOf<String>("14", "14"),
            arrayOf<String>("1500", "1999"),
            arrayOf<String>("20", "69"),
            arrayOf<String>("7000", "7499"),
            arrayOf<String>("750", "820"),
            arrayOf<String>("8210", "8249"),
            arrayOf<String>("825", "825"),
            arrayOf<String>("8260", "8289"),
            arrayOf<String>("829", "959"),
            arrayOf<String>("9600", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9967. Kyrgyz Republic
     */
    _9789967(
        9789967,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9968. Costa Rica
     */
    _9789968(
        9789968,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "49"),
            arrayOf<String>("500", "939"),
            arrayOf<String>("9400", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9970. Uganda
     */
    _9789970(
        9789970,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9971. Singapore
     */
    _9789971(
        9789971,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "5"),
            arrayOf<String>("60", "89"),
            arrayOf<String>("900", "989"),
            arrayOf<String>("9900", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9972. Peru
     */
    _9789972(
        9789972,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "09"),
            arrayOf<String>("1", "1"),
            arrayOf<String>("200", "249"),
            arrayOf<String>("2500", "2999"),
            arrayOf<String>("30", "59"),
            arrayOf<String>("600", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9973. Tunisia
     */
    _9789973(
        9789973,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "05"),
            arrayOf<String>("060", "089"),
            arrayOf<String>("0900", "0999"),
            arrayOf<String>("10", "69"),
            arrayOf<String>("700", "969"),
            arrayOf<String>("9700", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9974. Uruguay
     */
    _9789974(
        9789974,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "54"),
            arrayOf<String>("550", "749"),
            arrayOf<String>("7500", "8799"),
            arrayOf<String>("880", "909"),
            arrayOf<String>("91", "94"),
            arrayOf<String>("95", "99")
        ),
        4
    ),
    /**
     * Group: 978-9975. Moldova
     */
    _9789975(
        9789975,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("100", "299"),
            arrayOf<String>("3000", "3999"),
            arrayOf<String>("4000", "4499"),
            arrayOf<String>("45", "89"),
            arrayOf<String>("900", "949"),
            arrayOf<String>("9500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9976. Tanzania
     */
    _9789976(
        9789976,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("5000", "5899"),
            arrayOf<String>("59", "89"),
            arrayOf<String>("900", "989"),
            arrayOf<String>("9900", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9977. Costa Rica
     */
    _9789977(
        9789977,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "89"),
            arrayOf<String>("900", "989"),
            arrayOf<String>("9900", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9978. Ecuador
     */
    _9789978(
        9789978,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "29"),
            arrayOf<String>("300", "399"),
            arrayOf<String>("40", "94"),
            arrayOf<String>("950", "989"),
            arrayOf<String>("9900", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9979. Iceland
     */
    _9789979(
        9789979,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "64"),
            arrayOf<String>("650", "659"),
            arrayOf<String>("66", "75"),
            arrayOf<String>("760", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9980. Papua New Guinea
     */
    _9789980(
        9789980,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "3"),
            arrayOf<String>("40", "89"),
            arrayOf<String>("900", "989"),
            arrayOf<String>("9900", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9981. Morocco
     */
    _9789981(
        9789981,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "09"),
            arrayOf<String>("100", "159"),
            arrayOf<String>("1600", "1999"),
            arrayOf<String>("20", "79"),
            arrayOf<String>("800", "949"),
            arrayOf<String>("9500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9982. Zambia
     */
    _9789982(
        9789982,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "79"),
            arrayOf<String>("800", "989"),
            arrayOf<String>("9900", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9983. Gambia
     */
    _9789983(
        9789983,
        arrayOf<Array<String>>(
            arrayOf<String>("0000000", "7999999"),
            arrayOf<String>("80", "94"),
            arrayOf<String>("950", "989"),
            arrayOf<String>("9900", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9984. Latvia
     */
    _9789984(
        9789984,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "49"),
            arrayOf<String>("500", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9985. Estonia
     */
    _9789985(
        9789985,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "899"),
            arrayOf<String>("9000", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9986. Lithuania
     */
    _9789986(
        9789986,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "899"),
            arrayOf<String>("9000", "9399"),
            arrayOf<String>("940", "969"),
            arrayOf<String>("97", "99")
        ),
        4
    ),
    /**
     * Group: 978-9987. Tanzania
     */
    _9789987(
        9789987,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "39"),
            arrayOf<String>("400", "879"),
            arrayOf<String>("8800", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9988. Ghana
     */
    _9789988(
        9789988,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "54"),
            arrayOf<String>("550", "749"),
            arrayOf<String>("7500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-9989. Macedonia
     */
    _9789989(
        9789989,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("100", "199"),
            arrayOf<String>("2000", "2999"),
            arrayOf<String>("30", "59"),
            arrayOf<String>("600", "949"),
            arrayOf<String>("9500", "9999")
        ),
        4
    ),
    /**
     * Group: 978-99901. Bahrain
     */
    _97899901(
        97899901,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "49"),
            arrayOf<String>("500", "799"),
            arrayOf<String>("80", "99")
        ),
        3
    ),
    /**
     * Group: 978-99902. Reserved Agency
     */
    _97899902(97899902, arrayOf<Array<String>>(arrayOf<String>("0000000", "9999999")), 0),
    /**
     * Group: 978-99903. Mauritius
     */
    _97899903(
        97899903,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99904. Curaçao
     */
    _97899904(
        97899904,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "5"),
            arrayOf<String>("60", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99905. Bolivia
     */
    _97899905(
        97899905,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "3"),
            arrayOf<String>("40", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99906. Kuwait
     */
    _97899906(
        97899906,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "59"),
            arrayOf<String>("600", "699"),
            arrayOf<String>("70", "89"),
            arrayOf<String>("90", "94"),
            arrayOf<String>("950", "999")
        ),
        3
    ),
    /**
     * Group: 978-99908. Malawi
     */
    _97899908(
        97899908,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99909. Malta
     */
    _97899909(
        97899909,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "3"),
            arrayOf<String>("40", "94"),
            arrayOf<String>("950", "999")
        ),
        3
    ),
    /**
     * Group: 978-99910. Sierra Leone
     */
    _97899910(
        97899910,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99911. Lesotho
     */
    _97899911(
        97899911,
        arrayOf<Array<String>>(arrayOf<String>("00", "59"), arrayOf<String>("600", "999")),
        3
    ),
    /**
     * Group: 978-99912. Botswana
     */
    _97899912(
        97899912,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "3"),
            arrayOf<String>("400", "599"),
            arrayOf<String>("60", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99913. Andorra
     */
    _97899913(
        97899913,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "35"),
            arrayOf<String>("3600000", "5999999"),
            arrayOf<String>("600", "604"),
            arrayOf<String>("6050000", "9999999")
        ),
        3
    ),
    /**
     * Group: 978-99914. International NGO Publishers
     */
    _97899914(
        97899914,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "69"),
            arrayOf<String>("7", "7"),
            arrayOf<String>("80", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99915. Maldives
     */
    _97899915(
        97899915,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99916. Namibia
     */
    _97899916(
        97899916,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "69"),
            arrayOf<String>("700", "999")
        ),
        3
    ),
    /**
     * Group: 978-99917. Brunei Darussalam
     */
    _97899917(
        97899917,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99918. Faroe Islands
     */
    _97899918(
        97899918,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "3"),
            arrayOf<String>("40", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99919. Benin
     */
    _97899919(
        97899919,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("300", "399"),
            arrayOf<String>("40", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99920. Andorra
     */
    _97899920(
        97899920,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99921. Qatar
     */
    _97899921(
        97899921,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "69"),
            arrayOf<String>("700", "799"),
            arrayOf<String>("8", "8"),
            arrayOf<String>("90", "99")
        ),
        3
    ),
    /**
     * Group: 978-99922. Guatemala
     */
    _97899922(
        97899922,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "3"),
            arrayOf<String>("40", "69"),
            arrayOf<String>("700", "999")
        ),
        3
    ),
    /**
     * Group: 978-99923. El Salvador
     */
    _97899923(
        97899923,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99924. Nicaragua
     */
    _97899924(
        97899924,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99925. Paraguay
     */
    _97899925(
        97899925,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "3"),
            arrayOf<String>("40", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99926. Honduras
     */
    _97899926(
        97899926,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "59"),
            arrayOf<String>("600", "869"),
            arrayOf<String>("87", "89"),
            arrayOf<String>("90", "99")
        ),
        3
    ),
    /**
     * Group: 978-99927. Albania
     */
    _97899927(
        97899927,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "59"),
            arrayOf<String>("600", "999")
        ),
        3
    ),
    /**
     * Group: 978-99928. Georgia
     */
    _97899928(
        97899928,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99929. Mongolia
     */
    _97899929(
        97899929,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99930. Armenia
     */
    _97899930(
        97899930,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99931. Seychelles
     */
    _97899931(
        97899931,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99932. Malta
     */
    _97899932(
        97899932,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "59"),
            arrayOf<String>("600", "699"),
            arrayOf<String>("7", "7"),
            arrayOf<String>("80", "99")
        ),
        3
    ),
    /**
     * Group: 978-99933. Nepal
     */
    _97899933(
        97899933,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "59"),
            arrayOf<String>("600", "999")
        ),
        3
    ),
    /**
     * Group: 978-99934. Dominican Republic
     */
    _97899934(
        97899934,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99935. Haiti
     */
    _97899935(
        97899935,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "59"),
            arrayOf<String>("600", "699"),
            arrayOf<String>("7", "8"),
            arrayOf<String>("90", "99")
        ),
        3
    ),
    /**
     * Group: 978-99936. Bhutan
     */
    _97899936(
        97899936,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "59"),
            arrayOf<String>("600", "999")
        ),
        3
    ),
    /**
     * Group: 978-99937. Macau
     */
    _97899937(
        97899937,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "59"),
            arrayOf<String>("600", "999")
        ),
        3
    ),
    /**
     * Group: 978-99938. Srpska, Republic of
     */
    _97899938(
        97899938,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "59"),
            arrayOf<String>("600", "899"),
            arrayOf<String>("90", "99")
        ),
        3
    ),
    /**
     * Group: 978-99939. Guatemala
     */
    _97899939(
        97899939,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "5"),
            arrayOf<String>("60", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99940. Georgia
     */
    _97899940(
        97899940,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "69"),
            arrayOf<String>("700", "999")
        ),
        3
    ),
    /**
     * Group: 978-99941. Armenia
     */
    _97899941(
        97899941,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99942. Sudan
     */
    _97899942(
        97899942,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99943. Albania
     */
    _97899943(
        97899943,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "59"),
            arrayOf<String>("600", "999")
        ),
        3
    ),
    /**
     * Group: 978-99944. Ethiopia
     */
    _97899944(
        97899944,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99945. Namibia
     */
    _97899945(
        97899945,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "5"),
            arrayOf<String>("60", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99946. Nepal
     */
    _97899946(
        97899946,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "59"),
            arrayOf<String>("600", "999")
        ),
        3
    ),
    /**
     * Group: 978-99947. Tajikistan
     */
    _97899947(
        97899947,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "69"),
            arrayOf<String>("700", "999")
        ),
        3
    ),
    /**
     * Group: 978-99948. Eritrea
     */
    _97899948(
        97899948,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99949. Mauritius
     */
    _97899949(
        97899949,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99950. Cambodia
     */
    _97899950(
        97899950,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99951. Reserved Agency
     */
    _97899951(97899951, arrayOf<Array<String>>(arrayOf<String>("0000000", "9999999")), 0),
    /**
     * Group: 978-99952. Mali
     */
    _97899952(
        97899952,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99953. Paraguay
     */
    _97899953(
        97899953,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "79"),
            arrayOf<String>("800", "939"),
            arrayOf<String>("94", "99")
        ),
        3
    ),
    /**
     * Group: 978-99954. Bolivia
     */
    _97899954(
        97899954,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "69"),
            arrayOf<String>("700", "879"),
            arrayOf<String>("88", "99")
        ),
        3
    ),
    /**
     * Group: 978-99955. Srpska, Republic of
     */
    _97899955(
        97899955,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "59"),
            arrayOf<String>("600", "799"),
            arrayOf<String>("80", "99")
        ),
        3
    ),
    /**
     * Group: 978-99956. Albania
     */
    _97899956(
        97899956,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "59"),
            arrayOf<String>("600", "859"),
            arrayOf<String>("86", "99")
        ),
        3
    ),
    /**
     * Group: 978-99957. Malta
     */
    _97899957(
        97899957,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "79"),
            arrayOf<String>("800", "949"),
            arrayOf<String>("95", "99")
        ),
        3
    ),
    /**
     * Group: 978-99958. Bahrain
     */
    _97899958(
        97899958,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "93"),
            arrayOf<String>("940", "949"),
            arrayOf<String>("950", "999")
        ),
        3
    ),
    /**
     * Group: 978-99959. Luxembourg
     */
    _97899959(
        97899959,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "59"),
            arrayOf<String>("600", "999")
        ),
        3
    ),
    /**
     * Group: 978-99960. Malawi
     */
    _97899960(
        97899960,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "94"),
            arrayOf<String>("950", "999")
        ),
        3
    ),
    /**
     * Group: 978-99961. El Salvador
     */
    _97899961(
        97899961,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("300", "369"),
            arrayOf<String>("37", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99962. Mongolia
     */
    _97899962(
        97899962,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99963. Cambodia
     */
    _97899963(
        97899963,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "49"),
            arrayOf<String>("500", "919"),
            arrayOf<String>("92", "99")
        ),
        3
    ),
    /**
     * Group: 978-99964. Nicaragua
     */
    _97899964(
        97899964,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99965. Macau
     */
    _97899965(
        97899965,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("300", "359"),
            arrayOf<String>("36", "62"),
            arrayOf<String>("630", "999")
        ),
        3
    ),
    /**
     * Group: 978-99966. Kuwait
     */
    _97899966(
        97899966,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("30", "69"),
            arrayOf<String>("700", "799"),
            arrayOf<String>("80", "96"),
            arrayOf<String>("970", "999")
        ),
        3
    ),
    /**
     * Group: 978-99967. Paraguay
     */
    _97899967(
        97899967,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "59"),
            arrayOf<String>("600", "999")
        ),
        3
    ),
    /**
     * Group: 978-99968. Botswana
     */
    _97899968(
        97899968,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "3"),
            arrayOf<String>("400", "599"),
            arrayOf<String>("60", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99969. Oman
     */
    _97899969(
        97899969,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99970. Haiti
     */
    _97899970(
        97899970,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99971. Myanmar
     */
    _97899971(
        97899971,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "3"),
            arrayOf<String>("40", "84"),
            arrayOf<String>("850", "999")
        ),
        3
    ),
    /**
     * Group: 978-99972. Faroe Islands
     */
    _97899972(
        97899972,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "89"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99973. Mongolia
     */
    _97899973(
        97899973,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "3"),
            arrayOf<String>("40", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99974. Bolivia
     */
    _97899974(
        97899974,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("10", "25"),
            arrayOf<String>("260", "399"),
            arrayOf<String>("40", "63"),
            arrayOf<String>("640", "649"),
            arrayOf<String>("65", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99975. Tajikistan
     */
    _97899975(
        97899975,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "2"),
            arrayOf<String>("300", "399"),
            arrayOf<String>("40", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99976. Srpska, Republic of
     */
    _97899976(
        97899976,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("20", "59"),
            arrayOf<String>("600", "799"),
            arrayOf<String>("8000000", "8999999"),
            arrayOf<String>("900", "999")
        ),
        3
    ),
    /**
     * Group: 978-99977. Rwanda
     */
    _97899977(
        97899977,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("2000000", "3999999"),
            arrayOf<String>("40", "69"),
            arrayOf<String>("700", "799"),
            arrayOf<String>("8000000", "9999999")
        ),
        3
    ),
    /**
     * Group: 978-99978. Mongolia
     */
    _97899978(
        97899978,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "69"),
            arrayOf<String>("700", "999")
        ),
        3
    ),
    /**
     * Group: 978-99979. Honduras
     */
    _97899979(
        97899979,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "4"),
            arrayOf<String>("50", "79"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99980. Bhutan
     */
    _97899980(
        97899980,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("1000000", "2999999"),
            arrayOf<String>("30", "59"),
            arrayOf<String>("6000000", "7999999"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99981. Macau
     */
    _97899981(
        97899981,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "1"),
            arrayOf<String>("2000000", "2999999"),
            arrayOf<String>("30", "49"),
            arrayOf<String>("5000000", "7999999"),
            arrayOf<String>("800", "999")
        ),
        3
    ),
    /**
     * Group: 978-99982. Benin
     */
    _97899982(
        97899982,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("1000000", "4999999"),
            arrayOf<String>("50", "59"),
            arrayOf<String>("6000000", "8999999"),
            arrayOf<String>("900", "949"),
            arrayOf<String>("9500000", "9999999")
        ),
        3
    ),
    /**
     * Group: 978-99983. El Salvador
     */
    _97899983(
        97899983,
        arrayOf<Array<String>>(
            arrayOf<String>("0", "0"),
            arrayOf<String>("1000000", "4999999"),
            arrayOf<String>("50", "69"),
            arrayOf<String>("7000000", "9499999"),
            arrayOf<String>("950", "999")
        ),
        3
    ),
    /**
     * Group: 979-10. France
     */
    _97910(
        97910,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "19"),
            arrayOf<String>("200", "699"),
            arrayOf<String>("7000", "8999"),
            arrayOf<String>("90000", "97599"),
            arrayOf<String>("976000", "999999")
        ),
        6
    ),
    /**
     * Group: 979-11. Korea, Republic
     */
    _97911(
        97911,
        arrayOf<Array<String>>(
            arrayOf<String>("00", "24"),
            arrayOf<String>("250", "549"),
            arrayOf<String>("5500", "8499"),
            arrayOf<String>("85000", "94999"),
            arrayOf<String>("950000", "999999")
        ),
        6
    ),
    /**
     * Group: 979-12. Italy
     */
    _97912(
        97912,
        arrayOf<Array<String>>(
            arrayOf<String>("0000000", "1999999"),
            arrayOf<String>("200", "200"),
            arrayOf<String>("2010000", "9999999")
        ),
        3
    ),
    /**
     * Group: 979-8. United States
     */
    _9798(
        9798,
        arrayOf<Array<String>>(
            arrayOf<String>("00000000", "98499999"),
            arrayOf<String>("9850000", "9850009"),
            arrayOf<String>("98500100", "99999999")
        ),
        7
    );

    companion object {
        /**
         * Returns the group of the specified ISBN.
         *
         * @param isbn ISBN for the group wants to get.
         * @return the group of the specified ISBN or `null` if the ISBN is
         * not from any group.
         */
        fun getGroup(isbn: String): ISBNGroup? {
            var result: ISBNGroup? = null
            val length = isbn.length
            var i = 1
            while (result == null && i < length) {
                try {
                    result = valueOf('_'.toString() + isbn.substring(0, i))
                } catch (ex: IllegalArgumentException) {
                }
                i++
            }
            return result
        }
    }

}