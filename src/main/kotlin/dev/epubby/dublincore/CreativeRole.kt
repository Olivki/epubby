/*
 * Copyright 2019-2022 Oliver Berg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.epubby.dublincore

import dev.epubby.dublincore.CreativeRole.Companion.defaultRoles
import dev.epubby.utils.isLowerCase
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet

// http://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.2.6
/**
 * Represents a creative role that helped in some manner with the creation of some part of a epub.
 *
 * @property [code] The marc-relator code representing `this` role.
 *
 * This code can only be `3` characters long, or `6` in case the code is not a [default role][defaultRoles]. If it is a
 * custom code, then it will be prefixed with `oth.`.
 *
 * @property [name] A more human readable version of what the [code] is representing.
 *
 * Note that the `name` of a `CreativeRole` is never actually serialized in any manner, so this is merely here for
 * metadata and debugging purposes.
 */
// TODO: refactor this into a sealed class / sealed interface with all the properties replaced with implementations?
class CreativeRole private constructor(val code: String, val name: String?) {
    /**
     * Returns `true` if `this` is a custom role, meaning that it is not represented in the
     * [default roles][defaultRoles], otherwise `false`.
     */
    val isCustomRole: Boolean
        get() = code !in CACHE

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is CreativeRole -> false
        code != other.code -> false
        name != other.name -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = buildString {
        append("CreativeRole(")
        append("code='$code'")
        if (name != null) append(", name='$name'")
        append(")")
    }

    companion object {
        /**
         * Returns a set of all the roles that are defined by default.
         */
        @JvmStatic
        val defaultRoles: ImmutableSet<CreativeRole>
            get() = CACHE.values.toImmutableSet()

        private val CACHE: MutableMap<String, CreativeRole> = hashMapOf()

        private fun createConstant(code: String, name: String): CreativeRole {
            val role = CreativeRole(code, name)
            CACHE[code] = role
            return role
        }

        /**
         * Returns a new [creative role][CreativeRole] instance based on the given [code] and [name], or returns a
         * default role if the given [code] matches that of a role contained inside of the
         * [default roles][defaultRoles].
         *
         * Due to the nature of how this function works you will not always get an instance returned that perfectly
         * matches the parameters you entered, for example, say you invoke this with `of("art", "The Artist")` the
         * returned instance will *not* be `CreativeRole("art", "The Artist")` but rather
         * `CreativeRole("art", "Artist")` as the code `"art"` is associated with the default role [ARTIST].
         *
         * Note that any non-recognized `code` values will be prefixed with `'oth.'` before being created, meaning that
         * if you, for example, invoke this function with `code` set as `'öad'` the `code` of the returned instance will
         * be `'oth.öad'` and not `'öad'`. This will not happen if `code` is already prefixed with `'oth.'`.
         *
         * @throws [IllegalArgumentException] if the given [code] is malformed in some manner
         */
        @JvmStatic
        @JvmOverloads
        fun of(code: String, name: String? = null): CreativeRole {
            require(code.isLowerCase()) { "'code' must be all lowercase, was '${code}'." }
            require(code.length == 3 && !code.startsWith("oth.")) { "'code' must be exactly 3 characters long, was ${code.length} characters long." }
            require(code.length == 7 && code.startsWith("oth.")) { "custom codes must be exactly 7 characters long, was ${code.length} characters long." }
            return create(code, name)
        }

        // used internally for lenient parsing when deserializing from XML
        @JvmSynthetic
        internal fun create(_code: String, name: String? = null): CreativeRole {
            val code = _code.lowercase()
            return when (val value = CACHE[code]) {
                null -> CreativeRole(if (_code.startsWith("oth.")) _code else "oth.$_code", name)
                else -> value
            }
        }

        /**
         * A person, family, or organization contributing to a resource by shortening or condensing the original
         * work but leaving the nature and content of the original work substantially unchanged. For substantial
         * modifications that result in the creation of a new work, see author.
         */
        @JvmField
        val ABRIDGER: CreativeRole = createConstant("abr", "Abridger")

        /**
         * A performer contributing to an expression of a work by acting as a cast member or player in a musical or
         * dramatic presentation, etc.
         */
        @JvmField
        val ACTOR: CreativeRole = createConstant("act", "Actor")

        /**
         * A person or organization who 1) reworks a musical composition, usually for a different medium, or 2)
         * rewrites novels or stories for motion pictures or other audiovisual medium.
         */
        @JvmField
        val ADAPTER: CreativeRole = createConstant("adp", "Adapter")

        /**
         * A person, family, or organization to whom the correspondence in a work is addressed.
         */
        @JvmField
        val ADDRESSEE: CreativeRole = createConstant("rcp", "Addressee")

        /**
         * A person or organization that reviews, examines and interprets data or information in a specific area.
         */
        @JvmField
        val ANALYST: CreativeRole = createConstant("anl", "Analyst")

        /**
         * A person contributing to a moving image work or computer program by giving apparent movement to inanimate
         * objects or drawings. For the creator of the drawings that are animated, see artist.
         */
        @JvmField
        val ANIMATOR: CreativeRole = createConstant("anm", "Animator")

        /**
         * A person who makes manuscript annotations on an item.
         */
        @JvmField
        val ANNOTATOR: CreativeRole = createConstant("ann", "Annotator")

        /**
         * A person or organization who appeals a lower court's decision.
         */
        @JvmField
        val APPELLANT: CreativeRole = createConstant("apl", "Appellant")

        /**
         * A person or organization against whom an appeal is taken.
         */
        @JvmField
        val APPELLEE: CreativeRole = createConstant("ape", "Appellee")

        /**
         * A person or organization responsible for the submission of an application or who is named as eligible for
         * the results of the processing of the application *(e.g., bestowing of rights, reward, title, position)*.
         */
        @JvmField
        val APPLICANT: CreativeRole = createConstant("app", "Applicant")

        /**
         * A person, family, or organization responsible for creating an architectural design, including a pictorial
         * representation intended to show how a building, etc., will look when completed. It also oversees the
         * construction of structures.
         */
        @JvmField
        val ARCHITECT: CreativeRole = createConstant("arc", "Architect")

        /**
         * A person, family, or organization contributing to a musical work by rewriting the composition for a
         * medium of performance different from that for which the work was originally intended, or modifying the
         * work for the same medium of performance, etc., such that the musical substance of the original
         * composition remains essentially unchanged. For extensive modification that effectively results in the
         * creation of a new musical work, see composer.
         */
        @JvmField
        val ARRANGER: CreativeRole = createConstant("arr", "Arranger")

        /**
         * A person *(e.g., a painter or sculptor)* who makes copies of works of visual art.
         */
        @JvmField
        val ART_COPYIST: CreativeRole = createConstant("acp", "Art copyist")

        /**
         * A person contributing to a motion picture or television production by overseeing the artists and
         * craftspeople who build the sets.
         */
        @JvmField
        val ART_DIRECTOR: CreativeRole = createConstant("adi", "Art director")

        /**
         * A person, family, or organization responsible for creating a work by conceiving, and implementing, an
         * original graphic design, drawing, painting, etc. For epub illustrators, prefer [ILLUSTRATOR].
         */
        @JvmField
        val ARTIST: CreativeRole = createConstant("art", "Artist")

        /**
         * A person responsible for controlling the development of the artistic style of an entire production,
         * including the choice of works to be presented and selection of senior production staff.
         */
        @JvmField
        val ARTISTIC_DIRECTOR: CreativeRole = createConstant("ard", "Artistic director")

        /**
         * A person or organization to whom a license for printing or publishing has been transferred.
         */
        @JvmField
        val ASSIGNEE: CreativeRole = createConstant("asg", "Assignee")

        /**
         * A person or organization associated with or found in an item or collection, which cannot be determined to
         * be that of a [FORMER_OWNER] or other designated relationship indicative of provenance.
         */
        @JvmField
        val ASSOCIATED_NAME: CreativeRole = createConstant("asn", "Associated name")

        /**
         * An author, artist, etc., relating him/her to a resource for which there is or once was substantial
         * authority for designating that person as author, creator, etc. of the work.
         */
        @JvmField
        val ATTRIBUTED_NAME: CreativeRole = createConstant("att", "Attributed name")

        /**
         * A person or organization in charge of the estimation and public auctioning of goods, particularly books,
         * artistic works, etc.
         */
        @JvmField
        val AUCTIONEER: CreativeRole = createConstant("auc", "Auctioneer")

        /**
         * A person, family, or organization responsible for creating a work that is primarily textual in content,
         * regardless of media type *(e.g., printed text, spoken word, electronic text, tactile text)* or genre
         * *(e.g., poems, novels, screenplays, blogs)*. Use also for persons, etc., creating a new work by
         * paraphrasing, rewriting, or adapting works by another creator such that the modification has
         * substantially changed the nature and content of the original or changed the medium of expression.
         */
        @JvmField
        val AUTHOR: CreativeRole = createConstant("aut", "Author")

        /**
         * A person or organization whose work is largely quoted or extracted in works to which he or she did not
         * contribute directly. Such quotations are found particularly in exhibition catalogs, collections of
         * photographs, etc.
         */
        @JvmField
        val AUTHOR_IN_QUOTATIONS_OR_TEXT_ABSTRACTS: CreativeRole =
            createConstant("aqt", "Author in quotations or text abstracts")

        /**
         * A person or organization responsible for an afterword, postface, colophon, etc. but who is not the chief
         * author of a work.
         */
        @JvmField
        val AUTHOR_OF_AFTERWORD: CreativeRole = createConstant("aft", "Author of afterword, colophon, etc.")

        /**
         * A person or organization responsible for the dialog or spoken commentary for a screenplay or sound
         * recording.
         */
        @JvmField
        val AUTHOR_OF_DIALOG: CreativeRole = createConstant("aud", "Author of dialog")

        /**
         * A person or organization responsible for an introduction, preface, foreword, or other critical
         * introductory matter, but who is not the chief author.
         */
        @JvmField
        val AUTHOR_OF_INTRODUCTION: CreativeRole = createConstant("aui", "Author of introduction, etc.")

        /**
         * A person whose manuscript signature appears on an item.
         */
        @JvmField
        val AUTOGRAPHER: CreativeRole = createConstant("ato", "Autographer")

        /**
         * A person or organization responsible for a resource upon which the resource represented by the
         * bibliographic description is based. This may be appropriate for adaptations, sequels, continuations,
         * indexes, etc.
         */
        @JvmField
        val BIBLIOGRAPHIC_ANTECEDENT: CreativeRole = createConstant("ant", "Bibliographic antecedent")

        /**
         * A person who binds an item.
         */
        @JvmField
        val BINDER: CreativeRole = createConstant("bnd", "Binder")

        /**
         * A person or organization responsible for the binding design of a epub, including the type of binding, the
         * type of materials used, and any decorative aspects of the binding.
         */
        @JvmField
        val BINDING_DESIGNER: CreativeRole = createConstant("bdd", "Binding designer")

        /**
         * A person or organization responsible for writing a commendation or testimonial for a work, which appears
         * on or within the publication itself, frequently on the back or dust jacket of print publications or on
         * advertising material for all media.
         */
        @JvmField
        val BLURB_WRITER: CreativeRole = createConstant("blw", "Blurb writer")

        /**
         * A person or organization involved in manufacturing a manifestation by being responsible for the entire
         * graphic design of a epub, including arrangement of type and illustration, choice of materials, and
         * process used.
         */
        @JvmField
        val BOOK_DESIGNER: CreativeRole = createConstant("bkd", "Epub designer")

        /**
         * A person or organization responsible for the production of books and other print media.
         */
        @JvmField
        val BOOK_PRODUCER: CreativeRole = createConstant("bkp", "Epub producer")

        /**
         * A person or organization responsible for the design of flexible covers designed for or published with a
         * epub, including the type of materials used, and any decorative aspects of the bookjacket.
         */
        @JvmField
        val BOOKJACKET_DESIGNER: CreativeRole = createConstant("bjd", "Bookjacket designer")

        /**
         * A person or organization responsible for the design of a epub owner's identification label that is most
         * commonly pasted to the inside front cover of a epub.
         */
        @JvmField
        val BOOKPLATE_DESIGNER: CreativeRole = createConstant("bpd", "Bookplate designer")

        /**
         * A person or organization who makes books and other bibliographic materials available for purchase.
         * Interest in the materials is primarily lucrative.
         */
        @JvmField
        val BOOKSELLER: CreativeRole = createConstant("bsl", "Bookseller")

        /**
         * A person, family, or organization involved in manufacturing a resource by embossing Braille cells using a
         * stylus, special embossing printer, or other device.
         */
        @JvmField
        val BRAILLE_EMBOSSER: CreativeRole = createConstant("brl", "Braille embosser")

        /**
         * A person, family, or organization involved in broadcasting a resource to an audience via radio,
         * television, webcast, etc.
         */
        @JvmField
        val BROADCASTER: CreativeRole = createConstant("brd", "Broadcaster")

        /**
         * A person or organization who writes in an artistic hand, usually as a copyist and or engrosser.
         */
        @JvmField
        val CALLIGRAPHER: CreativeRole = createConstant("cll", "Calligrapher")

        /**
         * A person, family, or organization responsible for creating a map, atlas, globe, or other cartographic
         * work.
         */
        @JvmField
        val CARTOGRAPHER: CreativeRole = createConstant("ctg", "Cartographer")

        /**
         * A person, family, or organization involved in manufacturing a resource by pouring a liquid or molten
         * substance into a mold and leaving it to solidify to take the shape of the mold.
         */
        @JvmField
        val CASTER: CreativeRole = createConstant("cas", "Caster")

        /**
         * A person or organization who examines bibliographic resources for the purpose of suppressing parts deemed
         * objectionable on moral, political, military, or other grounds.
         */
        @JvmField
        val CENSOR: CreativeRole = createConstant("cns", "Censor")

        /**
         * A person responsible for creating or contributing to a work of movement.
         */
        @JvmField
        val CHOREOGRAPHER: CreativeRole = createConstant("chr", "Choreographer")

        /**
         * A person in charge of photographing a motion picture, who plans the technical aspets of lighting and
         * photographing of scenes, and often assists the director in the choice of angles, camera setups, and
         * lighting moods. He or she may also supervise the further processing of filmed material up to the
         * completion of the work print. Cinematographer is also referred to as director of photography. Do not
         * confuse with videographer.
         */
        @JvmField
        val CINEMATOGRAPHER: CreativeRole = createConstant("cng", "Cinematographer")

        /**
         * A person or organization for whom another person or organization is acting.
         */
        @JvmField
        val CLIENT: CreativeRole = createConstant("cli", "Client")

        /**
         * A curator who lists or inventories the items in an aggregate work such as a collection of items or works.
         */
        @JvmField
        val COLLECTION_REGISTRAR: CreativeRole = createConstant("cor", "Collection registrar")

        /**
         * A curator who brings together items from various sources that are then arranged, described, and cataloged
         * as a collection. A collector is neither the creator of the material nor a person to whom manuscripts in
         * the collection may have been addressed.
         */
        @JvmField
        val COLLECTOR: CreativeRole = createConstant("col", "Collector")

        /**
         * A person, family, or organization involved in manufacturing a manifestation of photographic prints from
         * film or other colloid that has ink-receptive and ink-repellent surfaces.
         */
        @JvmField
        val COLLOTYPER: CreativeRole = createConstant("clt", "Collotyper")

        /**
         * A person or organization responsible for applying color to drawings, prints, photographs, maps, moving
         * images, etc.
         */
        @JvmField
        val COLORIST: CreativeRole = createConstant("clr", "Colorist")

        /**
         * A performer contributing to a work by providing interpretation, analysis, or a discussion of the subject
         * matter on a recording, film, or other audiovisual medium.
         */
        @JvmField
        val COMMENTATOR: CreativeRole = createConstant("cmm", "Commentator")

        /**
         * A person or organization responsible for the commentary or explanatory notes about a text. For the writer
         * of manuscript annotations in a printed epub, use Annotator.
         */
        @JvmField
        val COMMENTATOR_FOR_WRITTEN_TEXT: CreativeRole = createConstant("cwt", "Commentator for written text")

        /**
         * A person, family, or organization responsible for creating a new work *(e.g., a bibliography, a
         * directory)* through the act of compilation, e.g., selecting, arranging, aggregating, and editing data,
         * information, etc.
         */
        @JvmField
        val COMPILER: CreativeRole = createConstant("com", "Compiler")

        /**
         * A person or organization who applies to the courts for redress, usually in an equity proceeding.
         */
        @JvmField
        val COMPLAINANT: CreativeRole = createConstant("cpl", "Complainant")

        /**
         * A complainant who takes an appeal from one court or jurisdiction to another to reverse the judgment,
         * usually in an equity proceeding.
         */
        @JvmField
        val COMPLAINANT_APPELLANT: CreativeRole = createConstant("cpt", "Complainant-appellant")

        /**
         * A complainant against whom an appeal is taken from one court or jurisdiction to another to reverse the
         * judgment, usually in an equity proceeding.
         */
        @JvmField
        val COMPLAINANT_APPELLEE: CreativeRole = createConstant("cpe", "Complainant-appellee")

        /**
         * A person, family, or organization responsible for creating or contributing to a musical resource by
         * adding music to a work that originally lacked it or supplements it.
         */
        @JvmField
        val COMPOSER: CreativeRole = createConstant("cmp", "Composer")

        /**
         * A person or organization responsible for the creation of metal slug, or molds made of other materials,
         * used to produce the text and images in printed matter.
         */
        @JvmField
        val COMPOSITOR: CreativeRole = createConstant("cmt", "Compositor")

        /**
         * A person or organization responsible for the original idea on which a work is based, this includes the
         * scientific author of an audio-visual item and the conceptor of an advertisement.
         */
        @JvmField
        val CONCEPTOR: CreativeRole = createConstant("ccp", "Conceptor")

        /**
         * A performer contributing to a musical resource by leading a performing group *(orchestra, chorus, opera,
         * etc.)* in a musical or dramatic presentation, etc.
         */
        @JvmField
        val CONDUCTOR: CreativeRole = createConstant("cnd", "Conductor")

        /**
         * A person or organization responsible for documenting, preserving, or treating printed or manuscript
         * material, works of art, artifacts, or other media.
         */
        @JvmField
        val CONSERVATOR: CreativeRole = createConstant("con", "Conservator")

        /**
         * A person or organization relevant to a resource, who is called upon for professional advice or services
         * in a specialized field of knowledge or training.
         */
        @JvmField
        val CONSULTANT: CreativeRole = createConstant("csl", "Consultant")

        /**
         * A person or organization relevant to a resource, who is engaged specifically to provide an intellectual
         * overview of a strategic or operational task and by analysis, specification, or instruction, to create or
         * propose a cost-effective course of action or solution.
         */
        @JvmField
        val CONSULTANT_TO_A_PROJECT: CreativeRole = createConstant("csp", "Consultant to a project")

        /**
         * A person*(s)* or organization who opposes, resists, or disputes, in a court of law, a claim, decision,
         * result, etc.
         */
        @JvmField
        val CONTESTANT: CreativeRole = createConstant("cos", "Contestant")

        /**
         * A contestant who takes an appeal from one court of law or jurisdiction to another to reverse the
         * judgment.
         */
        @JvmField
        val CONTESTANT_APPELLANT: CreativeRole = createConstant("cot", "Contestant-appellant")

        /**
         * A contestant against whom an appeal is taken from one court of law or jurisdiction to another to reverse
         * the judgment.
         */
        @JvmField
        val CONTESTANT_APPELLEE: CreativeRole = createConstant("coe", "Contestant-appellee")

        /**
         * A person*(s)* or organization defending a claim, decision, result, etc. being opposed, resisted, or
         * disputed in a court of law.
         */
        @JvmField
        val CONTESTEE: CreativeRole = createConstant("cts", "Contestee")

        /**
         * A contestee who takes an appeal from one court or jurisdiction to another to reverse the judgment.
         */
        @JvmField
        val CONTESTEE_APPELLANT: CreativeRole = createConstant("ctt", "Contestee-appellant")

        /**
         * A contestee against whom an appeal is taken from one court or jurisdiction to another to reverse the
         * judgment.
         */
        @JvmField
        val CONTESTEE_APPELLEE: CreativeRole = createConstant("cte", "Contestee-appellee")

        /**
         * A person or organization relevant to a resource, who enters into a contract with another person or
         * organization to perform a specific.
         */
        @JvmField
        val CONTRACTOR: CreativeRole = createConstant("ctr", "Contractor")

        /**
         * A person, family or organization responsible for making contributions to the resource. This includes
         * those whose work has been contributed to a larger work, such as an anthology, serial publication, or
         * other compilation of individual works. If a more specific role is available, prefer that, e.g. editor,
         * compiler, illustrator.
         */
        @JvmField
        val CONTRIBUTOR: CreativeRole = createConstant("ctb", "Contributor")

        /**
         * A person or organization listed as a copyright owner at the time of registration. Copyright can be
         * granted or later transferred to another person or organization, at which time the claimant becomes the
         * copyright holder.
         */
        @JvmField
        val COPYRIGHT_CLAIMANT: CreativeRole = createConstant("cpc", "Copyright claimant")

        /**
         * A person or organization to whom copy and legal rights have been granted or transferred for the
         * intellectual content of a work. The copyright holder, although not necessarily the creator of the work,
         * usually has the exclusive right to benefit financially from the sale and use of the work to which the
         * associated copyright protection applies.
         */
        @JvmField
        val COPYRIGHT_HOLDER: CreativeRole = createConstant("cph", "Copyright holder")

        /**
         * A person or organization who is a corrector of manuscripts, such as the scriptorium official who
         * corrected the work of a scribe. For printed matter, use Proofreader.
         */
        @JvmField
        val CORRECTOR: CreativeRole = createConstant("crr", "Corrector")

        /**
         * A person or organization who was either the writer or recipient of a letter or other communication.
         */
        @JvmField
        val CORRESPONDENT: CreativeRole = createConstant("crp", "Correspondent")

        /**
         * A person, family, or organization that designs the costumes for a moving image production or for a
         * musical or dramatic presentation or entertainment.
         */
        @JvmField
        val COSTUME_DESIGNER: CreativeRole = createConstant("cst", "Costume designer")

        /**
         * A court governed by court rules, regardless of their official nature *(e.g., laws, administrative
         * regulations)*.
         */
        @JvmField
        val COURT_GOVERNED: CreativeRole = createConstant("cou", "Court governed")

        /**
         * A person, family, or organization contributing to a resource by preparing a court's opinions for
         * publication.
         */
        @JvmField
        val COURT_REPORTER: CreativeRole = createConstant("crt", "Court reporter")

        /**
         * A person or organization responsible for the graphic design of a epub cover, album cover, slipcase, box,
         * container, etc. For a person or organization responsible for the graphic design of an entire epub, use
         * Epub designer; for epub jackets, use Bookjacket designer.
         */
        @JvmField
        val COVER_DESIGNER: CreativeRole = createConstant("cov", "Cover designer")

        /**
         * A person or organization responsible for the intellectual or artistic content of a resource.
         */
        @JvmField
        val CREATOR: CreativeRole = createConstant("cre", "Creator")

        /**
         * A person, family, or organization conceiving, aggregating, and/or organizing an exhibition, collection,
         * or other item.
         */
        @JvmField
        val CURATOR: CreativeRole = createConstant("cur", "Curator")

        /**
         * A performer who dances in a musical, dramatic, etc., presentation.
         */
        @JvmField
        val DANCER: CreativeRole = createConstant("dnc", "Dancer")

        /**
         * A person or organization that submits data for inclusion in a database or other collection of data.
         */
        @JvmField
        val DATA_CONTRIBUTOR: CreativeRole = createConstant("dtc", "Data contributor")

        /**
         * A person or organization responsible for managing databases or other data sources.
         */
        @JvmField
        val DATA_MANAGER: CreativeRole = createConstant("dtm", "Data manager")

        /**
         * A person, family, or organization to whom a resource is dedicated.
         */
        @JvmField
        val DEDICATEE: CreativeRole = createConstant("dte", "Dedicatee")

        /**
         * A person who writes a dedication, which may be a formal statement or in epistolary or verse form.
         */
        @JvmField
        val DEDICATOR: CreativeRole = createConstant("dto", "Dedicator")

        /**
         * A person or organization who is accused in a criminal proceeding or sued in a civil proceeding.
         */
        @JvmField
        val DEFENDANT: CreativeRole = createConstant("dfd", "Defendant")

        /**
         * A defendant who takes an appeal from one court or jurisdiction to another to reverse the judgment,
         * usually in a legal action.
         */
        @JvmField
        val DEFENDANT_APPELLANT: CreativeRole = createConstant("dft", "Defendant-appellant")

        /**
         * A defendant against whom an appeal is taken from one court or jurisdiction to another to reverse the
         * judgment, usually in a legal action.
         */
        @JvmField
        val DEFENDANT_APPELLEE: CreativeRole = createConstant("dfe", "Defendant-appellee")

        /**
         * A organization granting an academic degree.
         */
        @JvmField
        val DEGREE_GRANTING_INSTITUTION: CreativeRole = createConstant("dgg", "Degree granting institution")

        /**
         * A person overseeing a higher level academic degree.
         */
        @JvmField
        val DEGREE_SUPERVISOR: CreativeRole = createConstant("dgs", "Degree supervisor")

        /**
         * A person or organization executing technical drawings from others' designs.
         */
        @JvmField
        val DELINEATOR: CreativeRole = createConstant("dln", "Delineator")

        /**
         * An entity depicted or portrayed in a work, particularly in a work of art.
         */
        @JvmField
        val DEPICTED: CreativeRole = createConstant("dpc", "Depicted")

        /**
         * A current owner of an item who deposited the item into the custody of another person, family, or
         * organization, while still retaining ownership.
         */
        @JvmField
        val DEPOSITOR: CreativeRole = createConstant("dpt", "Depositor")

        /**
         * A person, family, or organization responsible for creating a design for an object.
         */
        @JvmField
        val DESIGNER: CreativeRole = createConstant("dsr", "Designer")

        /**
         * A person responsible for the general management and supervision of a filmed performance, a radio or
         * television program, etc.
         */
        @JvmField
        val DIRECTOR: CreativeRole = createConstant("drt", "Director")

        /**
         * A person who presents a thesis for a university or higher-level educational degree.
         */
        @JvmField
        val DISSERTANT: CreativeRole = createConstant("dis", "Dissertant")

        /**
         * A place from which a resource, e.g., a serial, is distributed.
         */
        @JvmField
        val DISTRIBUTION_PLACE: CreativeRole = createConstant("dbp", "Distribution place")

        /**
         * A person or organization that has exclusive or shared marketing rights for a resource.
         */
        @JvmField
        val DISTRIBUTOR: CreativeRole = createConstant("dst", "Distributor")

        /**
         * A former owner of an item who donated that item to another owner.
         */
        @JvmField
        val DONOR: CreativeRole = createConstant("dnr", "Donor")

        /**
         * A person, family, or organization contributing to a resource by an architect, inventor, etc., by making
         * detailed plans or drawings for buildings, ships, aircraft, machines, objects, etc.
         */
        @JvmField
        val DRAFTSMAN: CreativeRole = createConstant("drm", "Draftsman")

        /**
         * A person or organization to which authorship has been dubiously or incorrectly ascribed.
         */
        @JvmField
        val DUBIOUS_AUTHOR: CreativeRole = createConstant("dub", "Dubious author")

        /**
         * A person, family, or organization contributing to a resource by revising or elucidating the content,
         * e.g., adding an introduction, notes, or other critical matter. An editor may also prepare a resource for
         * production, publication, or distribution. For major revisions, adaptations, etc., that substantially
         * change the nature and content of the original work, resulting in a new work, see author.
         */
        @JvmField
        val EDITOR: CreativeRole = createConstant("edt", "Editor")

        /**
         * A person, family, or organization contributing to a collective or aggregate work by selecting and putting
         * together works, or parts of works, by one or more creators. For compilations of data, information, etc.,
         * that result in new works, see compiler.
         */
        @JvmField
        val EDITOR_OF_COMPILATION: CreativeRole = createConstant("edc", "Editor of compilation")

        /**
         * A person, family, or organization responsible for assembling, arranging, and trimming film, video, or
         * other moving image formats, including both visual and audio aspects.
         */
        @JvmField
        val EDITOR_OF_MOVING_IMAGE_WORK: CreativeRole = createConstant("edm", "Editor of moving image work")

        /**
         * A person responsible for setting up a lighting rig and focusing the lights for a production, and running
         * the lighting at a performance.
         */
        @JvmField
        val ELECTRICIAN: CreativeRole = createConstant("elg", "Electrician")

        /**
         * A person or organization who creates a duplicate printing surface by pressure molding and
         * electrodepositing of metal that is then backed up with lead for printing.
         */
        @JvmField
        val ELECTROTYPER: CreativeRole = createConstant("elt", "Electrotyper")

        /**
         * A jurisdiction enacting a law, regulation, constitution, court rule, etc.
         */
        @JvmField
        val ENACTING_JURISDICTION: CreativeRole = createConstant("enj", "Enacting jurisdiction")

        /**
         * A person or organization that is responsible for technical planning and design, particularly with
         * construction.
         */
        @JvmField
        val ENGINEER: CreativeRole = createConstant("eng", "Engineer")

        /**
         * A person or organization who cuts letters, figures, etc. on a surface, such as a wooden or metal plate
         * used for printing.
         */
        @JvmField
        val ENGRAVER: CreativeRole = createConstant("egr", "Engraver")

        /**
         * A person or organization who produces text or images for printing by subjecting metal, glass, or some
         * other surface to acid or the corrosive action of some other substance.
         */
        @JvmField
        val ETCHER: CreativeRole = createConstant("etr", "Etcher")

        /**
         * A place where an event such as a conference or a concert took place.
         */
        @JvmField
        val EVENT_PLACE: CreativeRole = createConstant("evp", "Event place")

        /**
         * A person or organization in charge of the description and appraisal of the value of goods, particularly
         * rare items, works of art, etc.
         */
        @JvmField
        val EXPERT: CreativeRole = createConstant("exp", "Expert")

        /**
         * A person or organization that executed the facsimile.
         */
        @JvmField
        val FACSIMILIST: CreativeRole = createConstant("fac", "Facsimilist")

        /**
         * A person or organization that manages or supervises the work done to collect raw data or do research in
         * an actual setting or environment *(typically applies to the natural and social sciences)*.
         */
        @JvmField
        val FIELD_DIRECTOR: CreativeRole = createConstant("fld", "Field director")

        /**
         * A director responsible for the general management and supervision of a filmed performance.
         */
        @JvmField
        val FILM_DIRECTOR: CreativeRole = createConstant("fmd", "Film director")

        /**
         * A person, family, or organization involved in distributing a moving image resource to theatres or other
         * distribution channels.
         */
        @JvmField
        val FILM_DISTRIBUTOR: CreativeRole = createConstant("fds", "Film distributor")

        /**
         * A person who, following the script and in creative cooperation with the Director, selects, arranges, and
         * assembles the filmed material, controls the synchronization of picture and sound, and participates in
         * other post-production tasks such as sound mixing and visual effects processing. Today, picture editing is
         * often performed digitally.
         */
        @JvmField
        val FILM_EDITOR: CreativeRole = createConstant("flm", "Film editor")

        /**
         * A producer responsible for most of the business aspects of a film.
         */
        @JvmField
        val FILM_PRODUCER: CreativeRole = createConstant("fmp", "Film producer")

        /**
         * A person, family or organization responsible for creating an independent or personal film. A filmmaker is
         * individually responsible for the conception and execution of all aspects of the film.
         */
        @JvmField
        val FILMMAKER: CreativeRole = createConstant("fmk", "Filmmaker")

        /**
         * A person or organization who is identified as the only party or the party of the first party. In the case
         * of transfer of rights, this is the assignor, transferor, licensor, grantor, etc. Multiple parties can be
         * named jointly as the first party.
         */
        @JvmField
        val FIRST_PARTY: CreativeRole = createConstant("fpy", "First party")

        /**
         * A person or organization who makes or imitates something of value or importance, especially with the
         * intent to defraud.
         */
        @JvmField
        val FORGER: CreativeRole = createConstant("frg", "Forger")

        /**
         * A person, family, or organization formerly having legal possession of an item.
         */
        @JvmField
        val FORMER_OWNER: CreativeRole = createConstant("fmo", "Former owner")

        /**
         * A person or organization that furnished financial support for the production of the work.
         */
        @JvmField
        val FUNDER: CreativeRole = createConstant("fnd", "Funder")

        /**
         * A person responsible for geographic information system *(GIS)* development and integration with global
         * positioning system data.
         */
        @JvmField
        val GEOGRAPHIC_INFORMATION_SPECIALIST: CreativeRole =
            createConstant("gis", "Geographic information specialist")

        /**
         * A person, family, or organization honored by a work or item *(e.g., the honoree of a festschrift, a
         * person to whom a copy is presented)*.
         */
        @JvmField
        val HONOREE: CreativeRole = createConstant("hnr", "Honoree")

        /**
         * A performer contributing to a resource by leading a program *(often broadcast)* that includes other
         * guests, performers, etc. *(e.g., talk show host)*.
         */
        @JvmField
        val HOST: CreativeRole = createConstant("hst", "Host")

        /**
         * An organization hosting the event, exhibit, conference, etc., which gave rise to a resource, but having
         * little or no responsibility for the content of the resource.
         */
        @JvmField
        val HOST_INSTITUTION: CreativeRole = createConstant("his", "Host institution")

        /**
         * A person providing decoration to a specific item using precious metals or color, often with elaborate
         * designs and motifs.
         */
        @JvmField
        val ILLUMINATOR: CreativeRole = createConstant("ilu", "Illuminator")

        /**
         * A person, family, or organization contributing to a resource by supplementing the primary content with
         * drawings, diagrams, photographs, etc. If the work is primarily the artistic content created by this
         * entity, use artist or photographer.
         */
        @JvmField
        val ILLUSTRATOR: CreativeRole = createConstant("ill", "Illustrator")

        /**
         * A person who has written a statement of dedication or gift.
         */
        @JvmField
        val INSCRIBER: CreativeRole = createConstant("ins", "Inscriber")

        /**
         * A performer contributing to a resource by playing a musical instrument.
         */
        @JvmField
        val INSTRUMENTALIST: CreativeRole = createConstant("itr", "Instrumentalist")

        /**
         * A person, family or organization responsible for creating or contributing to a resource by responding to
         * an interviewer, usually a reporter, pollster, or some other information gathering agent.
         */
        @JvmField
        val INTERVIEWEE: CreativeRole = createConstant("ive", "Interviewee")

        /**
         * A person, family, or organization responsible for creating or contributing to a resource by acting as an
         * interviewer, reporter, pollster, or some other information gathering agent.
         */
        @JvmField
        val INTERVIEWER: CreativeRole = createConstant("ivr", "Interviewer")

        /**
         * A person, family, or organization responsible for creating a new device or process.
         */
        @JvmField
        val INVENTOR: CreativeRole = createConstant("inv", "Inventor")

        /**
         * A person, family or organization issuing a work, such as an official organ of the body.
         */
        @JvmField
        val ISSUING_BODY: CreativeRole = createConstant("isb", "Issuing body")

        /**
         * A person who hears and decides on legal matters in court.
         */
        @JvmField
        val JUDGE: CreativeRole = createConstant("jud", "Judge")

        /**
         * A jurisdiction governed by a law, regulation, etc., that was enacted by another jurisdiction.
         */
        @JvmField
        val JURISDICTION_GOVERNED: CreativeRole = createConstant("jug", "Jurisdiction governed")

        /**
         * An organization that provides scientific analyses of material samples.
         */
        @JvmField
        val LABORATORY: CreativeRole = createConstant("lbr", "Laboratory")

        /**
         * A person or organization that manages or supervises work done in a controlled setting or environment.
         */
        @JvmField
        val LABORATORY_DIRECTOR: CreativeRole = createConstant("ldr", "Laboratory director")

        /**
         * An architect responsible for creating landscape works. This work involves coordinating the arrangement of
         * existing and proposed land features and structures.
         */
        @JvmField
        val LANDSCAPE_ARCHITECT: CreativeRole = createConstant("lsa", "Landscape architect")

        /**
         * A person or organization that takes primary responsibility for a particular activity or endeavor. May be
         * combined with another relator term or code to show the greater importance this person or organization has
         * regarding that particular role. If more than one relator is assigned to a heading, use the Lead relator
         * only if it applies to all the relators.
         */
        @JvmField
        val LEAD: CreativeRole = createConstant("led", "Lead")

        /**
         * A person or organization permitting the temporary use of a epub, manuscript, etc., such as for
         * photocopying or microfilming.
         */
        @JvmField
        val LENDER: CreativeRole = createConstant("len", "Lender")

        /**
         * A person or organization who files a libel in an ecclesiastical or admiralty case.
         */
        @JvmField
        val LIBELANT: CreativeRole = createConstant("lil", "Libelant")

        /**
         * A libelant who takes an appeal from one ecclesiastical court or admiralty to another to reverse the
         * judgment.
         */
        @JvmField
        val LIBELANT_APPELLANT: CreativeRole = createConstant("lit", "Libelant-appellant")

        /**
         * A libelant against whom an appeal is taken from one ecclesiastical court or admiralty to another to
         * reverse the judgment.
         */
        @JvmField
        val LIBELANT_APPELLEE: CreativeRole = createConstant("lie", "Libelant-appellee")

        /**
         * A person or organization against whom a libel has been filed in an ecclesiastical court or admiralty.
         */
        @JvmField
        val LIBELEE: CreativeRole = createConstant("lel", "Libelee")

        /**
         * A libelee who takes an appeal from one ecclesiastical court or admiralty to another to reverse the
         * judgment.
         */
        @JvmField
        val LIBELEE_APPELLANT: CreativeRole = createConstant("let", "Libelee-appellant")

        /**
         * A libelee against whom an appeal is taken from one ecclesiastical court or admiralty to another to
         * reverse the judgment.
         */
        @JvmField
        val LIBELEE_APPELLEE: CreativeRole = createConstant("lee", "Libelee-appellee")

        /**
         * An author of a libretto of an opera or other stage work, or an oratorio.
         */
        @JvmField
        val LIBRETTIST: CreativeRole = createConstant("lbt", "Librettist")

        /**
         * A person or organization who is an original recipient of the right to print or publish.
         */
        @JvmField
        val LICENSEE: CreativeRole = createConstant("lse", "Licensee")

        /**
         * A person or organization who is a signer of the license, imprimatur, etc.
         */
        @JvmField
        val LICENSOR: CreativeRole = createConstant("lso", "Licensor")

        /**
         * A person or organization who designs the lighting scheme for a theatrical presentation, entertainment,
         * motion picture, etc.
         */
        @JvmField
        val LIGHTING_DESIGNER: CreativeRole = createConstant("lgd", "Lighting designer")

        /**
         * A person or organization who prepares the stone or plate for lithographic printing, including a graphic
         * artist creating a design directly on the surface from which printing will be done.
         */
        @JvmField
        val LITHOGRAPHER: CreativeRole = createConstant("ltg", "Lithographer")

        /**
         * An author of the words of a non-dramatic musical work *(e.g. the text of a song)*, except for oratorios.
         */
        @JvmField
        val LYRICIST: CreativeRole = createConstant("lyr", "Lyricist")

        /**
         * The place of manufacture *(e.g., printing, duplicating, casting, etc.)* of a resource in a published
         * form.
         */
        @JvmField
        val MANUFACTURE_PLACE: CreativeRole = createConstant("mfp", "Manufacture place")

        /**
         * A person or organization responsible for printing, duplicating, casting, etc. a resource.
         */
        @JvmField
        val MANUFACTURER: CreativeRole = createConstant("mfr", "Manufacturer")

        /**
         * The entity responsible for marbling paper, cloth, leather, etc. used in construction of a resource.
         */
        @JvmField
        val MARBLER: CreativeRole = createConstant("mrb", "Marbler")

        /**
         * A person or organization performing the coding of SGML, HTML, or XML markup of metadata, text, etc.
         */
        @JvmField
        val MARKUP_EDITOR: CreativeRole = createConstant("mrk", "Markup editor")

        /**
         * A person held to be a channel of communication between the earthly world and a world.
         */
        @JvmField
        val MEDIUM: CreativeRole = createConstant("med", "Medium")

        /**
         * A person or organization primarily responsible for compiling and maintaining the original description of
         * a metadata set *(e.g., geospatial metadata set)*.
         */
        @JvmField
        val METADATA_CONTACT: CreativeRole = createConstant("mdc", "Metadata contact")

        /**
         * An engraver responsible for decorations, illustrations, letters, etc. cut on a metal surface for printing
         * or decoration.
         */
        @JvmField
        val METAL_ENGRAVER: CreativeRole = createConstant("mte", "Metal-engraver")

        /**
         * A person, family, or organization responsible for recording the minutes of a meeting.
         */
        @JvmField
        val MINUTE_TAKER: CreativeRole = createConstant("mtk", "Minute taker")

        /**
         * A performer contributing to a resource by leading a program *(often broadcast)* where topics are
         * discussed, usually with participation of experts in fields related to the discussion.
         */
        @JvmField
        val MODERATOR: CreativeRole = createConstant("mod", "Moderator")

        /**
         * A person or organization that supervises compliance with the contract and is responsible for the report
         * and controls its distribution. Sometimes referred to as the grantee, or controlling agency.
         */
        @JvmField
        val MONITOR: CreativeRole = createConstant("mon", "Monitor")

        /**
         * A person who transcribes or copies musical notation.
         */
        @JvmField
        val MUSIC_COPYIST: CreativeRole = createConstant("mcp", "Music copyist")

        /**
         * A person who coordinates the activities of the composer, the sound editor, and sound mixers for a moving
         * image production or for a musical or dramatic presentation or entertainment.
         */
        @JvmField
        val MUSICAL_DIRECTOR: CreativeRole = createConstant("msd", "Musical director")

        /**
         * A person or organization who performs music or contributes to the musical content of a work when it is
         * not possible or desirable to identify the function more precisely.
         */
        @JvmField
        val MUSICIAN: CreativeRole = createConstant("mus", "Musician")

        /**
         * A performer contributing to a resource by reading or speaking in order to give an account of an act,
         * occurrence, course of events, etc.
         */
        @JvmField
        val NARRATOR: CreativeRole = createConstant("nrt", "Narrator")

        /**
         * A performer contributing to an expression of a work by appearing on screen in nonfiction moving image
         * materials or introductions to fiction moving image materials to provide contextual or background
         * information. Use when another term *(e.g., narrator, host)* is either not applicable or not desired.
         */
        @JvmField
        val ONSCREEN_PRESENTER: CreativeRole = createConstant("osp", "Onscreen presenter")

        /**
         * A person or organization responsible for opposing a thesis or dissertation.
         */
        @JvmField
        val OPPONENT: CreativeRole = createConstant("opn", "Opponent")

        /**
         * A person, family, or organization organizing the exhibit, event, conference, etc., which gave rise to a
         * resource.
         */
        @JvmField
        val ORGANIZER: CreativeRole = createConstant("orm", "Organizer")

        /**
         * A person or organization performing the work, i.e., the name of a person or organization associated with
         * the intellectual content of the work. This category does not include the publisher or personal
         * affiliation, or sponsor except where it is also the corporate author.
         */
        @JvmField
        val ORIGINATOR: CreativeRole = createConstant("org", "Originator")

        /**
         * A role that has no equivalent in the MARC list.
         */
        @JvmField
        val OTHER: CreativeRole = createConstant("oth", "Other")

        /**
         * A person, family, or organization that currently owns an item or collection, i.e.has legal possession of
         * a resource.
         */
        @JvmField
        val OWNER: CreativeRole = createConstant("own", "Owner")

        /**
         * A performer contributing to a resource by participating in a program *(often broadcast)* where topics are
         * discussed, usually with participation of experts in fields related to the discussion.
         */
        @JvmField
        val PANELIST: CreativeRole = createConstant("pan", "Panelist")

        /**
         * A person or organization responsible for the production of paper, usually from wood, cloth, or other
         * fibrous material.
         */
        @JvmField
        val PAPERMAKER: CreativeRole = createConstant("ppm", "Papermaker")

        /**
         * A person or organization that applied for a patent.
         */
        @JvmField
        val PATENT_APPLICANT: CreativeRole = createConstant("pta", "Patent applicant")

        /**
         * A person or organization that was granted the patent referred to by the item.
         */
        @JvmField
        val PATENT_HOLDER: CreativeRole = createConstant("pth", "Patent holder")

        /**
         * A person or organization responsible for commissioning a work. Usually a patron uses his or her means or
         * influence to support the work of artists, writers, etc. This includes those who commission and pay for
         * individual works.
         */
        @JvmField
        val PATRON: CreativeRole = createConstant("pat", "Patron")

        /**
         * A person contributing to a resource by performing music, acting, dancing, speaking, etc., often in a
         * musical or dramatic presentation, etc. If specific codes are used, [PERFORMER] is used for a person whose
         * principal skill is not known or specified.
         */
        @JvmField
        val PERFORMER: CreativeRole = createConstant("prf", "Performer")

        /**
         * An organization *(usually a government agency)* that issues permits under which work is accomplished.
         */
        @JvmField
        val PERMITTING_AGENCY: CreativeRole = createConstant("pma", "Permitting agency")

        /**
         * A person, family, or organization responsible for creating a photographic work.
         */
        @JvmField
        val PHOTOGRAPHER: CreativeRole = createConstant("pht", "Photographer")

        /**
         * A person or organization who brings a suit in a civil proceeding.
         */
        @JvmField
        val PLAINTIFF: CreativeRole = createConstant("ptf", "Plaintiff")

        /**
         * A plaintiff who takes an appeal from one court or jurisdiction to another to reverse the judgment,
         * usually in a legal proceeding.
         */
        @JvmField
        val PLAINTIFF_APPELLANT: CreativeRole = createConstant("ptt", "Plaintiff-appellant")

        /**
         * A plaintiff against whom an appeal is taken from one court or jurisdiction to another to reverse the
         * judgment, usually in a legal proceeding.
         */
        @JvmField
        val PLAINTIFF_APPELLEE: CreativeRole = createConstant("pte", "Plaintiff-appellee")

        /**
         * A person, family, or organization involved in manufacturing a manifestation by preparing plates used in
         * the production of printed images and/or text.
         */
        @JvmField
        val PLATEMAKER: CreativeRole = createConstant("plt", "Platemaker")

        /**
         * A person who is the faculty moderator of an academic disputation, normally proposing a thesis and
         * participating in the ensuing disputation.
         */
        @JvmField
        val PRAESES: CreativeRole = createConstant("pra", "Praeses")

        /**
         * A person or organization mentioned in an “X presents” credit for moving image materials and who is
         * associated with production, finance, or distribution in some way. A vanity credit; in early years,
         * normally the head of a studio.
         */
        @JvmField
        val PRESENTER: CreativeRole = createConstant("pre", "Presenter")

        /**
         * A person, family, or organization involved in manufacturing a manifestation of printed text, notated
         * music, etc., from type or plates, such as a epub, newspaper, magazine, broadside, score, etc.
         */
        @JvmField
        val PRINTER: CreativeRole = createConstant("prt", "Printer")

        /**
         * A person or organization who prints illustrations from plates.
         */
        @JvmField
        val PRINTER_OF_PLATES: CreativeRole = createConstant("pop", "Printer of plates")

        /**
         * A person or organization who makes a relief, intaglio, or planographic printing surface.
         */
        @JvmField
        val PRINTMAKER: CreativeRole = createConstant("prm", "Printmaker")

        /**
         * A person or organization primarily responsible for performing or initiating a process, such as is done
         * with the collection of metadata sets.
         */
        @JvmField
        val PROCESS_CONTACT: CreativeRole = createConstant("prc", "Process contact")

        /**
         * A person, family, or organization responsible for most of the business aspects of a production for
         * screen, audio recording, television, webcast, etc. The producer is generally responsible for fund
         * raising, managing the production, hiring key personnel, arranging for distributors, etc.
         */
        @JvmField
        val PRODUCER: CreativeRole = createConstant("pro", "Producer")

        /**
         * An organization that is responsible for financial, technical, and organizational management of a
         * production for stage, screen, audio recording, television, webcast, etc.
         */
        @JvmField
        val PRODUCTION_COMPANY: CreativeRole = createConstant("prn", "Production company")

        /**
         * A person or organization responsible for designing the overall visual appearance of a moving image
         * production.
         */
        @JvmField
        val PRODUCTION_DESIGNER: CreativeRole = createConstant("prs", "Production designer")

        /**
         * A person responsible for all technical and business matters in a production.
         */
        @JvmField
        val PRODUCTION_MANAGER: CreativeRole = createConstant("pmn", "Production manager")

        /**
         * A person or organization associated with the production *(props, lighting, special effects, etc.)* of a
         * musical or dramatic presentation or entertainment.
         */
        @JvmField
        val PRODUCTION_PERSONNEL: CreativeRole = createConstant("prd", "Production personnel")

        /**
         * The place of production *(e.g., inscription, fabrication, construction, etc.)* of a resource in an
         * unpublished form.
         */
        @JvmField
        val PRODUCTION_PLACE: CreativeRole = createConstant("prp", "Production place")

        /**
         * A person, family, or organization responsible for creating a computer program.
         */
        @JvmField
        val PROGRAMMER: CreativeRole = createConstant("prg", "Programmer")

        /**
         * A person or organization with primary responsibility for all essential aspects of a project, has overall
         * responsibility for managing projects, or provides overall direction to a project manager.
         */
        @JvmField
        val PROJECT_DIRECTOR: CreativeRole = createConstant("pdr", "Project director")

        /**
         * A person who corrects printed matter. For manuscripts, use [CORRECTOR].
         */
        @JvmField
        val PROOFREADER: CreativeRole = createConstant("pfr", "Proofreader")

        /**
         * A person or organization who produces, publishes, manufactures, or distributes a resource if specific
         * codes are not desired *(e.g. [MANUFACTURER], [PUBLISHER])*.
         */
        @JvmField
        val PROVIDER: CreativeRole = createConstant("prv", "Provider")

        /**
         * The place where a resource is published.
         */
        @JvmField
        val PUBLICATION_PLACE: CreativeRole = createConstant("pup", "Publication place")

        /**
         * A person or organization responsible for publishing, releasing, or issuing a resource.
         */
        @JvmField
        val PUBLISHER: CreativeRole = createConstant("pbl", "Publisher")

        /**
         * A person or organization who presides over the elaboration of a collective work to ensure its coherence
         * or continuity. This includes editors-in-chief, literary editors, editors of series, etc.
         */
        @JvmField
        val PUBLISHING_DIRECTOR: CreativeRole = createConstant("pbd", "Publishing director")

        /**
         * A performer contributing to a resource by manipulating, controlling, or directing puppets or marionettes
         * in a moving image production or a musical or dramatic presentation or entertainment.
         */
        @JvmField
        val PUPPETEER: CreativeRole = createConstant("ppt", "Puppeteer")

        /**
         * A director responsible for the general management and supervision of a radio program.
         */
        @JvmField
        val RADIO_DIRECTOR: CreativeRole = createConstant("rdd", "Radio director")

        /**
         * A producer responsible for most of the business aspects of a radio program.
         */
        @JvmField
        val RADIO_PRODUCER: CreativeRole = createConstant("rpc", "Radio producer")

        /**
         * A person contributing to a resource by supervising the technical aspects of a sound or video recording
         * session.
         */
        @JvmField
        val RECORDING_ENGINEER: CreativeRole = createConstant("rce", "Recording engineer")

        /**
         * A person or organization who uses a recording device to capture sounds and/or video during a recording
         * session, including field recordings of natural sounds, folkloric events, music, etc.
         */
        @JvmField
        val RECORDIST: CreativeRole = createConstant("rcd", "Recordist")

        /**
         * A person or organization who writes or develops the framework for an item without being intellectually
         * responsible for its content.
         */
        @JvmField
        val REDAKTOR: CreativeRole = createConstant("red", "Redaktor")

        /**
         * A person or organization who prepares drawings of architectural designs *(i.e., renderings)* in accurate,
         * representational perspective to show what the project will look like when completed.
         */
        @JvmField
        val RENDERER: CreativeRole = createConstant("ren", "Renderer")

        /**
         * A person or organization who writes or presents reports of news or current events on air or in print.
         */
        @JvmField
        val REPORTER: CreativeRole = createConstant("rpt", "Reporter")

        /**
         * An organization that hosts data or material culture objects and provides services to promote long term,
         * consistent and shared use of those data or objects.
         */
        @JvmField
        val REPOSITORY: CreativeRole = createConstant("rps", "Repository")

        /**
         * A person who directed or managed a research project.
         */
        @JvmField
        val RESEARCH_TEAM_HEAD: CreativeRole = createConstant("rth", "Research team head")

        /**
         * A person who participated in a research project but whose role did not involve direction or management of
         * it.
         */
        @JvmField
        val RESEARCH_TEAM_MEMBER: CreativeRole = createConstant("rtm", "Research team member")

        /**
         * A person or organization responsible for performing research.
         */
        @JvmField
        val RESEARCHER: CreativeRole = createConstant("res", "Researcher")

        /**
         * A person or organization who makes an answer to the courts pursuant to an application for redress
         * *(usually in an equity proceeding)* or a candidate for a degree who defends or opposes a thesis provided
         * by the praeses in an academic disputation.
         */
        @JvmField
        val RESPONDENT: CreativeRole = createConstant("rsp", "Respondent")

        /**
         * A respondent who takes an appeal from one court or jurisdiction to another to reverse the judgment,
         * usually in an equity proceeding.
         */
        @JvmField
        val RESPONDENT_APPELLANT: CreativeRole = createConstant("rst", "Respondent-appellant")

        /**
         * A respondent against whom an appeal is taken from one court or jurisdiction to another to reverse the
         * judgment, usually in an equity proceeding.
         */
        @JvmField
        val RESPONDENT_APPELLEE: CreativeRole = createConstant("rse", "Respondent-appellee")

        /**
         * A person or organization legally responsible for the content of the published material.
         */
        @JvmField
        val RESPONSIBLE_PARTY: CreativeRole = createConstant("rpy", "Responsible party")

        /**
         * A person or organization, other than the original choreographer or director, responsible for restaging a
         * choreographic or dramatic work and who contributes minimal new content.
         */
        @JvmField
        val RESTAGER: CreativeRole = createConstant("rsg", "Restager")

        /**
         * A person, family, or organization responsible for the set of technical, editorial, and intellectual
         * procedures aimed at compensating for the degradation of an item by bringing it back to a state as close
         * as possible to its original condition.
         */
        @JvmField
        val RESTORATIONIST: CreativeRole = createConstant("rsr", "Restorationist")

        /**
         * A person or organization responsible for the review of a epub, motion picture, performance, etc.
         */
        @JvmField
        val REVIEWER: CreativeRole = createConstant("rev", "Reviewer")

        /**
         * A person or organization responsible for parts of a work, often headings or opening parts of a
         * manuscript, that appear in a distinctive color, usually red.
         */
        @JvmField
        val RUBRICATOR: CreativeRole = createConstant("rbr", "Rubricator")

        /**
         * A person or organization who is the author of a motion picture screenplay, generally the person who wrote
         * the scenarios for a motion picture during the silent era.
         */
        @JvmField
        val SCENARIST: CreativeRole = createConstant("sce", "Scenarist")

        /**
         * A person or organization who brings scientific, pedagogical, or historical competence to the conception
         * and realization on a work, particularly in the case of audio-visual items.
         */
        @JvmField
        val SCIENTIFIC_ADVISOR: CreativeRole = createConstant("sad", "Scientific advisor")

        /**
         * An author of a screenplay, script, or scene.
         */
        @JvmField
        val SCREENWRITER: CreativeRole = createConstant("aus", "Screenwriter")

        /**
         * A person who is an amanuensis and for a writer of manuscripts proper. For a person who makes
         * pen-facsimiles, use [FACSIMILIST].
         */
        @JvmField
        val SCRIBE: CreativeRole = createConstant("scr", "Scribe")

        /**
         * An artist responsible for creating a three-dimensional work by modeling, carving, or similar technique.
         */
        @JvmField
        val SCULPTOR: CreativeRole = createConstant("scl", "Sculptor")

        /**
         * A person or organization who is identified as the party of the second part. In the case of transfer of
         * right, this is the assignee, transferee, licensee, grantee, etc. Multiple parties can be named jointly as
         * the second party.
         */
        @JvmField
        val SECOND_PARTY: CreativeRole = createConstant("spy", "Second party")

        /**
         * A person or organization who is a recorder, redactor, or other person responsible for expressing the
         * views of a organization.
         */
        @JvmField
        val SECRETARY: CreativeRole = createConstant("sec", "Secretary")

        /**
         * A former owner of an item who sold that item to another owner.
         */
        @JvmField
        val SELLER: CreativeRole = createConstant("sll", "Seller")

        /**
         * A person who translates the rough sketches of the art director into actual architectural structures for a
         * theatrical presentation, entertainment, motion picture, etc. Set designers draw the detailed guides and
         * specifications for building the set.
         */
        @JvmField
        val SET_DESIGNER: CreativeRole = createConstant("std", "Set designer")

        /**
         * An entity in which the activity or plot of a work takes place, e.g. a geographic place, a time period, a
         * building, an event.
         */
        @JvmField
        val SETTING: CreativeRole = createConstant("stg", "Setting")

        /**
         * A person whose signature appears without a presentation or other statement indicative of provenance. When
         * there is a presentation statement, use [INSCRIBER].
         */
        @JvmField
        val SIGNER: CreativeRole = createConstant("sgn", "Signer")

        /**
         * A performer contributing to a resource by using his/her/their voice, with or without instrumental
         * accompaniment, to produce music. A singer's performance may or may not include actual words.
         */
        @JvmField
        val SINGER: CreativeRole = createConstant("sng", "Singer")

        /**
         * A person who produces and reproduces the sound score *(both live and recorded)*, the installation of
         * microphones, the setting of sound levels, and the coordination of sources of sound for a production.
         */
        @JvmField
        val SOUND_DESIGNER: CreativeRole = createConstant("sds", "Sound designer")

        /**
         * A performer contributing to a resource by speaking words, such as a lecture, speech, etc.
         */
        @JvmField
        val SPEAKER: CreativeRole = createConstant("spk", "Speaker")

        /**
         * A person, family, or organization sponsoring some aspect of a resource, e.g., funding research,
         * sponsoring an event.
         */
        @JvmField
        val SPONSOR: CreativeRole = createConstant("spn", "Sponsor")

        /**
         * A person or organization contributing to a stage resource through the overall management and supervision
         * of a performance.
         */
        @JvmField
        val STAGE_DIRECTOR: CreativeRole = createConstant("sgd", "Stage director")

        /**
         * A person who is in charge of everything that occurs on a performance stage, and who acts as chief of all
         * crews and assistant to a director during rehearsals.
         */
        @JvmField
        val STAGE_MANAGER: CreativeRole = createConstant("stm", "Stage manager")

        /**
         * An organization responsible for the development or enforcement of a standard.
         */
        @JvmField
        val STANDARDS_BODY: CreativeRole = createConstant("stn", "Standards body")

        /**
         * A person or organization who creates a new plate for printing by molding or copying another printing
         * surface.
         */
        @JvmField
        val STEREOTYPER: CreativeRole = createConstant("str", "Stereotyper")

        /**
         * A performer contributing to a resource by relaying a creator's original story with dramatic or theatrical
         * interpretation.
         */
        @JvmField
        val STORYTELLER: CreativeRole = createConstant("stl", "Storyteller")

        /**
         * A person or organization that supports *(by allocating facilities, staff, or other resources)* a project,
         * program, meeting, event, data objects, material culture objects, or other entities capable of support.
         */
        @JvmField
        val SUPPORTING_HOST: CreativeRole = createConstant("sht", "Supporting host")

        /**
         * A person, family, or organization contributing to a cartographic resource by providing measurements or
         * dimensional relationships for the geographic area represented.
         */
        @JvmField
        val SURVEYOR: CreativeRole = createConstant("srv", "Surveyor")

        /**
         * A performer contributing to a resource by giving instruction or providing a demonstration.
         */
        @JvmField
        val TEACHER: CreativeRole = createConstant("tch", "Teacher")

        /**
         * A person who is ultimately in charge of scenery, props, lights and sound for a production.
         */
        @JvmField
        val TECHNICAL_DIRECTOR: CreativeRole = createConstant("tcd", "Technical director")

        /**
         * A director responsible for the general management and supervision of a television program.
         */
        @JvmField
        val TELEVISION_DIRECTOR: CreativeRole = createConstant("tld", "Television director")

        /**
         * A producer responsible for most of the business aspects of a television program.
         */
        @JvmField
        val TELEVISION_PRODUCER: CreativeRole = createConstant("tlp", "Television producer")

        /**
         * A person under whose supervision a degree candidate develops and presents a thesis, mémoire, or text of a
         * dissertation.
         */
        @JvmField
        val THESIS_ADVISOR: CreativeRole = createConstant("ths", "Thesis advisor")

        /**
         * A person, family, or organization contributing to a resource by changing it from one system of notation
         * to another. For a work transcribed for a different instrument or performing group, see [ARRANGER]. For
         * makers of pen-facsimiles, use [FACSIMILIST].
         */
        @JvmField
        val TRANSCRIBER: CreativeRole = createConstant("trc", "Transcriber")

        /**
         * A person or organization who renders a text from one language into another, or from an older form of a
         * language into the modern form.
         */
        @JvmField
        val TRANSLATOR: CreativeRole = createConstant("trl", "Translator")

        /**
         * A person or organization who designs the type face used in a particular item.
         */
        @JvmField
        val TYPE_DESIGNER: CreativeRole = createConstant("tyd", "Type designer")

        /**
         * A person or organization primarily responsible for choice and arrangement of type used in an item. If the
         * typographer is also responsible for other aspects of the graphic design of a epub *(e.g.,
         * [BOOK_DESIGNER])*, codes for both functions may be needed.
         */
        @JvmField
        val TYPOGRAPHER: CreativeRole = createConstant("tyg", "Typographer")

        /**
         * A place where a university that is associated with a resource is located, for example, a university where
         * an academic dissertation or thesis was presented.
         */
        @JvmField
        val UNIVERSITY_PLACE: CreativeRole = createConstant("uvp", "University place")

        /**
         * A person in charge of a video production, e.g. the video recording of a stage production as opposed to a
         * commercial motion picture. The videographer may be the camera operator or may supervise one or more
         * camera operators. Do not confuse with cinematographer.
         */
        @JvmField
        val VIDEOGRAPHER: CreativeRole = createConstant("vdg", "Videographer")

        /**
         * An actor contributing to a resource by providing the voice for characters in radio and audio productions
         * and for animated characters in moving image works, as well as by providing voice overs in radio and
         * television commercials, dubbed resources, etc.
         */
        @JvmField
        val VOICE_ACTOR: CreativeRole = createConstant("vac", "Voice actor")

        /**
         * Use for a person who verifies the truthfulness of an event or action.
         */
        @JvmField
        val WITNESS: CreativeRole = createConstant("wit", "Witness")

        /**
         * A person or organization who makes prints by cutting the image in relief on the end-grain of a wood
         * block.
         */
        @JvmField
        val WOOD_ENGRAVER: CreativeRole = createConstant("wde", "Wood engraver")

        /**
         * A person or organization who makes prints by cutting the image in relief on the plank side of a wood
         * block.
         */
        @JvmField
        val WOODCUTTER: CreativeRole = createConstant("wdc", "Woodcutter")

        /**
         * A person or organization who writes significant material which accompanies a sound recording or other
         * audiovisual material.
         */
        @JvmField
        val WRITER_OF_ACCOMPANYING_MATERIAL: CreativeRole =
            createConstant("wam", "Writer of accompanying material")

        /**
         * A person, family, or organization contributing to an expression of a work by providing an interpretation
         * or critical explanation of the original work.
         */
        @JvmField
        val WRITER_OF_ADDED_COMMENTARY: CreativeRole = createConstant("wac", "Writer of added commentary")

        /**
         * A writer of words added to an expression of a musical work. For lyric writing in collaboration with a
         * composer to form an original work, see lyricist.
         */
        @JvmField
        val WRITER_OF_ADDED_LYRICS: CreativeRole = createConstant("wal", "Writer of added lyrics")

        /**
         * A person, family, or organization contributing to a non-textual resource by providing text for the
         * non-textual work *(e.g., writing captions for photographs, descriptions of maps)*.
         */
        @JvmField
        val WRITER_OF_ADDED_TEXT: CreativeRole = createConstant("wat", "Writer of added text")

        /**
         * A person, family, or organization contributing to a resource by providing an introduction to the original
         * work.
         */
        @JvmField
        val WRITER_OF_INTRODUCTION: CreativeRole = createConstant("win", "Writer of introduction")

        /**
         * A person, family, or organization contributing to a resource by providing a preface to the original work.
         */
        @JvmField
        val WRITER_OF_PREFACE: CreativeRole = createConstant("wpr", "Writer of preface")

        /**
         * A person, family, or organization contributing to a resource by providing supplementary textual content
         * *(e.g., an introduction, a preface)* to the original work.
         */
        @JvmField
        val WRITER_OF_SUPPLEMENTARY_TEXTUAL_CONTENT: CreativeRole =
            createConstant("wst", "Writer of supplementary textual content")
    }
}