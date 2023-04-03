/*
 * Copyright 2019-2023 Oliver Berg
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

/**
 * Represents a default creative role that helped in some manner with the creation of some part of an epub.
 *
 * @property [code] The [marc-relator code](https://www.loc.gov/marc/relators/) representing the role.
 *
 * The code of a default role can only be max `3` characters long.
 *
 * This code can only be `3` characters long, or `6` in case the code is not a [default role][defaultRoles]. If it is a
 * custom code, then it will be prefixed with `oth.`.
 */
public sealed class DefaultCreativeRole(override val code: String, override val name: String) : CreativeRole {
    final override fun toString(): String = "DefaultCreativeRole(code='$code', name='$name')"

    /**
     * A person, family, or organization contributing to a resource by shortening or condensing the original work but leaving the nature and content of the original work substantially unchanged. For substantial modifications that result in the creation of a new work, see author.
     */
    public object Abridger : DefaultCreativeRole("abr", "Abridger")

    /**
     * A performer contributing to an expression of a work by acting as a cast member or player in a musical or dramatic presentation, etc.
     */
    public object Actor : DefaultCreativeRole("act", "Actor")

    /**
     * A person or organization who 1) reworks a musical composition, usually for a different medium, or 2) rewrites novels or stories for motion pictures or other audiovisual medium.
     */
    public object Adapter : DefaultCreativeRole("adp", "Adapter")

    /**
     * A person, family, or organization to whom the correspondence in a work is addressed.
     *
     * Also known as: *Recipient*.
     */
    public object Addressee : DefaultCreativeRole("rcp", "Addressee")

    /**
     * A person or organization that reviews, examines and interprets data or information in a specific area.
     */
    public object Analyst : DefaultCreativeRole("anl", "Analyst")

    /**
     * A person contributing to a moving image work or computer program by giving apparent movement to inanimate objects or drawings. For the creator of the drawings that are animated, see artist.
     */
    public object Animator : DefaultCreativeRole("anm", "Animator")

    /**
     * A person who makes manuscript annotations on an item.
     */
    public object Annotator : DefaultCreativeRole("ann", "Annotator")

    /**
     * A person or organization who appeals a lower court's decision.
     */
    public object Appellant : DefaultCreativeRole("apl", "Appellant")

    /**
     * A person or organization against whom an appeal is taken.
     */
    public object Appellee : DefaultCreativeRole("ape", "Appellee")

    /**
     * A person or organization responsible for the submission of an application or who is named as eligible for the results of the processing of the application *(e.g., bestowing of rights, reward, title, position)*.
     */
    public object Applicant : DefaultCreativeRole("app", "Applicant")

    /**
     * A person, family, or organization responsible for creating an architectural design, including a pictorial representation intended to show how a building, etc., will look when completed. It also oversees the construction of structures.
     */
    public object Architect : DefaultCreativeRole("arc", "Architect")

    /**
     * A person, family, or organization contributing to a musical work by rewriting the composition for a medium of performance different from that for which the work was originally intended, or modifying the work for the same medium of performance, etc., such that the musical substance of the original composition remains essentially unchanged. For extensive modification that effectively results in the creation of a new musical work, see composer.
     *
     * Also known as: *Arranger Of Music*.
     */
    public object Arranger : DefaultCreativeRole("arr", "Arranger")

    /**
     * A person *(e.g., a painter or sculptor)* who makes copies of works of visual art.
     */
    public object ArtCopyist : DefaultCreativeRole("acp", "Art copyist")

    /**
     * A person contributing to a motion picture or television production by overseeing the artists and craftspeople who build the sets.
     */
    public object ArtDirector : DefaultCreativeRole("adi", "Art director")

    /**
     * A person, family, or organization responsible for creating a work by conceiving, and implementing, an original graphic design, drawing, painting, etc. For book illustrators, prefer [Illustrator].
     *
     * Also known as: *Graphic Technician*.
     */
    public object Artist : DefaultCreativeRole("art", "Artist")

    /**
     * A person responsible for controlling the development of the artistic style of an entire production, including the choice of works to be presented and selection of senior production staff.
     */
    public object ArtisticDirector : DefaultCreativeRole("ard", "Artistic director")

    /**
     * A person or organization to whom a license for printing or publishing has been transferred.
     */
    public object Assignee : DefaultCreativeRole("asg", "Assignee")

    /**
     * A person or organization associated with or found in an item or collection, which cannot be determined to be that of a [FormerOwner] or other designated relationship indicative of provenance.
     */
    public object AssociatedName : DefaultCreativeRole("asn", "Associated name")

    /**
     * An author, artist, etc., relating him/her to a resource for which there is or once was substantial authority for designating that person as author, creator, etc. of the work.
     *
     * Also known as: *Supposed Name*.
     */
    public object AttributedName : DefaultCreativeRole("att", "Attributed name")

    /**
     * A person or organization in charge of the estimation and public auctioning of goods, particularly books, artistic works, etc.
     */
    public object Auctioneer : DefaultCreativeRole("auc", "Auctioneer")

    /**
     * A person, family, or organization responsible for creating a work that is primarily textual in content, regardless of media type *(e.g., printed text, spoken word, electronic text, tactile text)* or genre *(e.g., poems, novels, screenplays, blogs)*. Use also for persons, etc., creating a new work by paraphrasing, rewriting, or adapting works by another creator such that the modification has substantially changed the nature and content of the original or changed the medium of expression.
     *
     * Also known as: *Joint Author*.
     */
    public object Author : DefaultCreativeRole("aut", "Author")

    /**
     * A person or organization whose work is largely quoted or extracted in works to which he or she did not contribute directly. Such quotations are found particularly in exhibition catalogs, collections of photographs, etc.
     */
    public object AuthorInQuotationsOrTextAbstracts :
        DefaultCreativeRole("aqt", "Author in quotations or text abstracts")

    /**
     * A person or organization responsible for an afterword, postface, colophon, etc. but who is not the chief author of a work.
     */
    public object AuthorOfAfterword : DefaultCreativeRole("aft", "Author of afterword")

    /**
     * A person or organization responsible for the dialog or spoken commentary for a screenplay or sound recording.
     */
    public object AuthorOfDialog : DefaultCreativeRole("aud", "Author of dialog")

    /**
     * A person or organization responsible for an introduction, preface, foreword, or other critical introductory matter, but who is not the chief author.
     */
    public object AuthorOfIntroduction : DefaultCreativeRole("aui", "Author of introduction")

    /**
     * A person whose manuscript signature appears on an item.
     */
    public object Autographer : DefaultCreativeRole("ato", "Autographer")

    /**
     * A person or organization responsible for a resource upon which the resource represented by the bibliographic description is based. This may be appropriate for adaptations, sequels, continuations, indexes, etc.
     */
    public object BibliographicAntecedent : DefaultCreativeRole("ant", "Bibliographic antecedent")

    /**
     * A person who binds an item.
     */
    public object Binder : DefaultCreativeRole("bnd", "Binder")

    /**
     * A person or organization responsible for the binding design of a book, including the type of binding, the type of materials used, and any decorative aspects of the binding.
     *
     * Also known as: *Designer Of Binding*.
     */
    public object BindingDesigner : DefaultCreativeRole("bdd", "Binding designer")

    /**
     * A person or organization responsible for writing a commendation or testimonial for a work, which appears on or within the publication itself, frequently on the back or dust jacket of print publications or on advertising material for all media.
     */
    public object BlurbWriter : DefaultCreativeRole("blw", "Blurb writer")

    /**
     * A person or organization involved in manufacturing a manifestation by being responsible for the entire graphic design of a book, including arrangement of type and illustration, choice of materials, and process used.
     *
     * Also known as: *Designer Of Book*, *Designer Of E Book*.
     */
    public object BookDesigner : DefaultCreativeRole("bkd", "Book designer")

    /**
     * A person or organization responsible for the production of books and other print media.
     *
     * Also known as: *Producer Of Book*.
     */
    public object BookProducer : DefaultCreativeRole("bkp", "Book producer")

    /**
     * A person or organization responsible for the design of flexible covers designed for or published with a book, including the type of materials used, and any decorative aspects of the bookjacket.
     *
     * Also known as: *Designer Of Bookjacket*.
     */
    public object BookjacketDesigner : DefaultCreativeRole("bjd", "Bookjacket designer")

    /**
     * A person or organization responsible for the design of a book owner's identification label that is most commonly pasted to the inside front cover of a book.
     */
    public object BookplateDesigner : DefaultCreativeRole("bpd", "Bookplate designer")

    /**
     * A person or organization who makes books and other bibliographic materials available for purchase. Interest in the materials is primarily lucrative.
     */
    public object Bookseller : DefaultCreativeRole("bsl", "Bookseller")

    /**
     * A person, family, or organization involved in manufacturing a resource by embossing Braille cells using a stylus, special embossing printer, or other device.
     */
    public object BrailleEmbosser : DefaultCreativeRole("brl", "Braille embosser")

    /**
     * A person, family, or organization involved in broadcasting a resource to an audience via radio, television, webcast, etc.
     */
    public object Broadcaster : DefaultCreativeRole("brd", "Broadcaster")

    /**
     * A person or organization who writes in an artistic hand, usually as a copyist and or engrosser.
     */
    public object Calligrapher : DefaultCreativeRole("cll", "Calligrapher")

    /**
     * A person, family, or organization responsible for creating a map, atlas, globe, or other cartographic work.
     */
    public object Cartographer : DefaultCreativeRole("ctg", "Cartographer")

    /**
     * A person, family, or organization involved in manufacturing a resource by pouring a liquid or molten substance into a mold and leaving it to solidify to take the shape of the mold.
     */
    public object Caster : DefaultCreativeRole("cas", "Caster")

    /**
     * A person or organization who examines bibliographic resources for the purpose of suppressing parts deemed objectionable on moral, political, military, or other grounds.
     *
     * Also known as: *Bowdlerizer*, *Expurgator*.
     */
    public object Censor : DefaultCreativeRole("cns", "Censor")

    /**
     * A person responsible for creating or contributing to a work of movement.
     */
    public object Choreographer : DefaultCreativeRole("chr", "Choreographer")

    /**
     * A person in charge of photographing a motion picture, who plans the technical aspets of lighting and photographing of scenes, and often assists the director in the choice of angles, camera setups, and lighting moods. He or she may also supervise the further processing of filmed material up to the completion of the work print. Cinematographer is also referred to as director of photography. Do not confuse with videographer.
     *
     * Also known as: *Director Of Photography*.
     */
    public object Cinematographer : DefaultCreativeRole("cng", "Cinematographer")

    /**
     * A person or organization for whom another person or organization is acting.
     */
    public object Client : DefaultCreativeRole("cli", "Client")

    /**
     * A curator who lists or inventories the items in an aggregate work such as a collection of items or works.
     */
    public object CollectionRegistrar : DefaultCreativeRole("cor", "Collection registrar")

    /**
     * A curator who brings together items from various sources that are then arranged, described, and cataloged as a collection. A collector is neither the creator of the material nor a person to whom manuscripts in the collection may have been addressed.
     */
    public object Collector : DefaultCreativeRole("col", "Collector")

    /**
     * A person, family, or organization involved in manufacturing a manifestation of photographic prints from film or other colloid that has ink-receptive and ink-repellent surfaces.
     */
    public object Collotyper : DefaultCreativeRole("clt", "Collotyper")

    /**
     * A person or organization responsible for applying color to drawings, prints, photographs, maps, moving images, etc.
     */
    public object Colorist : DefaultCreativeRole("clr", "Colorist")

    /**
     * A performer contributing to a work by providing interpretation, analysis, or a discussion of the subject matter on a recording, film, or other audiovisual medium.
     */
    public object Commentator : DefaultCreativeRole("cmm", "Commentator")

    /**
     * A person or organization responsible for the commentary or explanatory notes about a text. For the writer of manuscript annotations in a printed book, use Annotator.
     */
    public object CommentatorForWrittenText : DefaultCreativeRole("cwt", "Commentator for written text")

    /**
     * A person, family, or organization responsible for creating a new work *(e.g., a bibliography, a directory)* through the act of compilation, e.g., selecting, arranging, aggregating, and editing data, information, etc.
     */
    public object Compiler : DefaultCreativeRole("com", "Compiler")

    /**
     * A person or organization who applies to the courts for redress, usually in an equity proceeding.
     */
    public object Complainant : DefaultCreativeRole("cpl", "Complainant")

    /**
     * A complainant who takes an appeal from one court or jurisdiction to another to reverse the judgment, usually in an equity proceeding.
     */
    public object ComplainantAppellant : DefaultCreativeRole("cpt", "Complainant-appellant")

    /**
     * A complainant against whom an appeal is taken from one court or jurisdiction to another to reverse the judgment, usually in an equity proceeding.
     */
    public object ComplainantAppellee : DefaultCreativeRole("cpe", "Complainant-appellee")

    /**
     * A person, family, or organization responsible for creating or contributing to a musical resource by adding music to a work that originally lacked it or supplements it.
     */
    public object Composer : DefaultCreativeRole("cmp", "Composer")

    /**
     * A person or organization responsible for the creation of metal slug, or molds made of other materials, used to produce the text and images in printed matter.
     *
     * Also known as: *Typesetter*.
     */
    public object Compositor : DefaultCreativeRole("cmt", "Compositor")

    /**
     * A person or organization responsible for the original idea on which a work is based, this includes the scientific author of an audio-visual item and the conceptor of an advertisement.
     */
    public object Conceptor : DefaultCreativeRole("ccp", "Conceptor")

    /**
     * A performer contributing to a musical resource by leading a performing group *(orchestra, chorus, opera, etc.)* in a musical or dramatic presentation, etc.
     */
    public object Conductor : DefaultCreativeRole("cnd", "Conductor")

    /**
     * A person or organization responsible for documenting, preserving, or treating printed or manuscript material, works of art, artifacts, or other media.
     *
     * Also known as: *Preservationist*.
     */
    public object Conservator : DefaultCreativeRole("con", "Conservator")

    /**
     * A person or organization relevant to a resource, who is called upon for professional advice or services in a specialized field of knowledge or training.
     */
    public object Consultant : DefaultCreativeRole("csl", "Consultant")

    /**
     * A person or organization relevant to a resource, who is engaged specifically to provide an intellectual overview of a strategic or operational task and by analysis, specification, or instruction, to create or propose a cost-effective course of action or solution.
     */
    public object ConsultantToAProject : DefaultCreativeRole("csp", "Consultant to a project")

    /**
     * A person*(s)* or organization who opposes, resists, or disputes, in a court of law, a claim, decision, result, etc.
     */
    public object Contestant : DefaultCreativeRole("cos", "Contestant")

    /**
     * A contestant who takes an appeal from one court of law or jurisdiction to another to reverse the judgment.
     */
    public object ContestantAppellant : DefaultCreativeRole("cot", "Contestant-appellant")

    /**
     * A contestant against whom an appeal is taken from one court of law or jurisdiction to another to reverse the judgment.
     */
    public object ContestantAppellee : DefaultCreativeRole("coe", "Contestant-appellee")

    /**
     * A person*(s)* or organization defending a claim, decision, result, etc. being opposed, resisted, or disputed in a court of law.
     */
    public object Contestee : DefaultCreativeRole("cts", "Contestee")

    /**
     * A contestee who takes an appeal from one court or jurisdiction to another to reverse the judgment.
     */
    public object ContesteeAppellant : DefaultCreativeRole("ctt", "Contestee-appellant")

    /**
     * A contestee against whom an appeal is taken from one court or jurisdiction to another to reverse the judgment.
     */
    public object ContesteeAppellee : DefaultCreativeRole("cte", "Contestee-appellee")

    /**
     * A person or organization relevant to a resource, who enters into a contract with another person or organization to perform a specific task.
     */
    public object Contractor : DefaultCreativeRole("ctr", "Contractor")

    /**
     * A person, family or organization responsible for making contributions to the resource. This includes those whose work has been contributed to a larger work, such as an anthology, serial publication, or other compilation of individual works. If a more specific role is available, prefer that, e.g. editor, compiler, illustrator.
     *
     * Also known as: *Collaborator*.
     */
    public object Contributor : DefaultCreativeRole("ctb", "Contributor")

    /**
     * A person or organization listed as a copyright owner at the time of registration. Copyright can be granted or later transferred to another person or organization, at which time the claimant becomes the copyright holder.
     */
    public object CopyrightClaimant : DefaultCreativeRole("cpc", "Copyright claimant")

    /**
     * A person or organization to whom copy and legal rights have been granted or transferred for the intellectual content of a work. The copyright holder, although not necessarily the creator of the work, usually has the exclusive right to benefit financially from the sale and use of the work to which the associated copyright protection applies.
     */
    public object CopyrightHolder : DefaultCreativeRole("cph", "Copyright holder")

    /**
     * A person or organization who is a corrector of manuscripts, such as the scriptorium official who corrected the work of a scribe. For printed matter, use Proofreader.
     */
    public object Corrector : DefaultCreativeRole("crr", "Corrector")

    /**
     * A person or organization who was either the writer or recipient of a letter or other communication.
     */
    public object Correspondent : DefaultCreativeRole("crp", "Correspondent")

    /**
     * A person, family, or organization that designs the costumes for a moving image production or for a musical or dramatic presentation or entertainment.
     */
    public object CostumeDesigner : DefaultCreativeRole("cst", "Costume designer")

    /**
     * A court governed by court rules, regardless of their official nature *(e.g., laws, administrative regulations)*.
     */
    public object CourtGoverned : DefaultCreativeRole("cou", "Court governed")

    /**
     * A person, family, or organization contributing to a resource by preparing a court's opinions for publication.
     */
    public object CourtReporter : DefaultCreativeRole("crt", "Court reporter")

    /**
     * A person or organization responsible for the graphic design of a book cover, album cover, slipcase, box, container, etc. For a person or organization responsible for the graphic design of an entire book, use Book designer; for book jackets, use Bookjacket designer.
     *
     * Also known as: *Designer Of Cover*.
     */
    public object CoverDesigner : DefaultCreativeRole("cov", "Cover designer")

    /**
     * A person or organization responsible for the intellectual or artistic content of a resource.
     */
    public object Creator : DefaultCreativeRole("cre", "Creator")

    /**
     * A person, family, or organization conceiving, aggregating, and/or organizing an exhibition, collection, or other item.
     *
     * Also known as: *Curator Of An Exhibition*.
     */
    public object Curator : DefaultCreativeRole("cur", "Curator")

    /**
     * A performer who dances in a musical, dramatic, etc., presentation.
     */
    public object Dancer : DefaultCreativeRole("dnc", "Dancer")

    /**
     * A person or organization that submits data for inclusion in a database or other collection of data.
     */
    public object DataContributor : DefaultCreativeRole("dtc", "Data contributor")

    /**
     * A person or organization responsible for managing databases or other data sources.
     */
    public object DataManager : DefaultCreativeRole("dtm", "Data manager")

    /**
     * A person, family, or organization to whom a resource is dedicated.
     *
     * Also known as: *Dedicatee Of Item*.
     */
    public object Dedicatee : DefaultCreativeRole("dte", "Dedicatee")

    /**
     * A person who writes a dedication, which may be a formal statement or in epistolary or verse form.
     */
    public object Dedicator : DefaultCreativeRole("dto", "Dedicator")

    /**
     * A person or organization who is accused in a criminal proceeding or sued in a civil proceeding.
     */
    public object Defendant : DefaultCreativeRole("dfd", "Defendant")

    /**
     * A defendant who takes an appeal from one court or jurisdiction to another to reverse the judgment, usually in a legal action.
     */
    public object DefendantAppellant : DefaultCreativeRole("dft", "Defendant-appellant")

    /**
     * A defendant against whom an appeal is taken from one court or jurisdiction to another to reverse the judgment, usually in a legal action.
     */
    public object DefendantAppellee : DefaultCreativeRole("dfe", "Defendant-appellee")

    /**
     * A person who is part of a committee that considers the merit of a thesis, dissertation, or other submission by an academic degree candidate.
     */
    public object DegreeCommitteeMember : DefaultCreativeRole("dgc", "Degree committee member")

    /**
     * A organization granting an academic degree.
     *
     * Also known as: *Degree Grantor*.
     */
    public object DegreeGrantingInstitution : DefaultCreativeRole("dgg", "Degree granting institution")

    /**
     * A person overseeing a higher level academic degree.
     */
    public object DegreeSupervisor : DefaultCreativeRole("dgs", "Degree supervisor")

    /**
     * A person or organization executing technical drawings from others' designs.
     */
    public object Delineator : DefaultCreativeRole("dln", "Delineator")

    /**
     * An entity depicted or portrayed in a work, particularly in a work of art.
     */
    public object Depicted : DefaultCreativeRole("dpc", "Depicted")

    /**
     * A current owner of an item who deposited the item into the custody of another person, family, or organization, while still retaining ownership.
     */
    public object Depositor : DefaultCreativeRole("dpt", "Depositor")

    /**
     * A person, family, or organization responsible for creating a design for an object.
     */
    public object Designer : DefaultCreativeRole("dsr", "Designer")

    /**
     * A person responsible for the general management and supervision of a filmed performance, a radio or television program, etc.
     */
    public object Director : DefaultCreativeRole("drt", "Director")

    /**
     * A person who presents a thesis for a university or higher-level educational degree.
     */
    public object Dissertant : DefaultCreativeRole("dis", "Dissertant")

    /**
     * A place from which a resource, e.g., a serial, is distributed.
     */
    public object DistributionPlace : DefaultCreativeRole("dbp", "Distribution place")

    /**
     * A person or organization that has exclusive or shared marketing rights for a resource.
     */
    public object Distributor : DefaultCreativeRole("dst", "Distributor")

    /**
     * A former owner of an item who donated that item to another owner.
     */
    public object Donor : DefaultCreativeRole("dnr", "Donor")

    /**
     * A person, family, or organization contributing to a resource by an architect, inventor, etc., by making detailed plans or drawings for buildings, ships, aircraft, machines, objects, etc.
     *
     * Also known as: *Technical Draftsman*.
     */
    public object Draftsman : DefaultCreativeRole("drm", "Draftsman")

    /**
     * A person or organization to which authorship has been dubiously or incorrectly ascribed.
     */
    public object DubiousAuthor : DefaultCreativeRole("dub", "Dubious author")

    /**
     * A person, family, or organization contributing to a resource by revising or elucidating the content, e.g., adding an introduction, notes, or other critical matter. An editor may also prepare a resource for production, publication, or distribution. For major revisions, adaptations, etc., that substantially change the nature and content of the original work, resulting in a new work, see author.
     */
    public object Editor : DefaultCreativeRole("edt", "Editor")

    /**
     * A person, family, or organization contributing to a collective or aggregate work by selecting and putting together works, or parts of works, by one or more creators. For compilations of data, information, etc., that result in new works, see compiler.
     */
    public object EditorOfCompilation : DefaultCreativeRole("edc", "Editor of compilation")

    /**
     * A person, family, or organization responsible for assembling, arranging, and trimming film, video, or other moving image formats, including both visual and audio aspects.
     *
     * Also known as: *Moving Image Work Editor*.
     */
    public object EditorOfMovingImageWork : DefaultCreativeRole("edm", "Editor of moving image work")

    /**
     * A person responsible for setting up a lighting rig and focusing the lights for a production, and running the lighting at a performance.
     *
     * Also known as: *Chief Electrician*, *House Electrician*, *Master Electrician*.
     */
    public object Electrician : DefaultCreativeRole("elg", "Electrician")

    /**
     * A person or organization who creates a duplicate printing surface by pressure molding and electrodepositing of metal that is then backed up with lead for printing.
     */
    public object Electrotyper : DefaultCreativeRole("elt", "Electrotyper")

    /**
     * A jurisdiction enacting a law, regulation, constitution, court rule, etc.
     */
    public object EnactingJurisdiction : DefaultCreativeRole("enj", "Enacting jurisdiction")

    /**
     * A person or organization that is responsible for technical planning and design, particularly with construction.
     */
    public object Engineer : DefaultCreativeRole("eng", "Engineer")

    /**
     * A person or organization who cuts letters, figures, etc. on a surface, such as a wooden or metal plate used for printing.
     */
    public object Engraver : DefaultCreativeRole("egr", "Engraver")

    /**
     * A person or organization who produces text or images for printing by subjecting metal, glass, or some other surface to acid or the corrosive action of some other substance.
     */
    public object Etcher : DefaultCreativeRole("etr", "Etcher")

    /**
     * A place where an event such as a conference or a concert took place.
     */
    public object EventPlace : DefaultCreativeRole("evp", "Event place")

    /**
     * A person or organization in charge of the description and appraisal of the value of goods, particularly rare items, works of art, etc.
     *
     * Also known as: *Appraiser*.
     */
    public object Expert : DefaultCreativeRole("exp", "Expert")

    /**
     * A person or organization that executed the facsimile.
     *
     * Also known as: *Copier*.
     */
    public object Facsimilist : DefaultCreativeRole("fac", "Facsimilist")

    /**
     * A person or organization that manages or supervises the work done to collect raw data or do research in an actual setting or environment *(typically applies to the natural and social sciences)*.
     */
    public object FieldDirector : DefaultCreativeRole("fld", "Field director")

    /**
     * A director responsible for the general management and supervision of a filmed performance.
     */
    public object FilmDirector : DefaultCreativeRole("fmd", "Film director")

    /**
     * A person, family, or organization involved in distributing a moving image resource to theatres or other distribution channels.
     */
    public object FilmDistributor : DefaultCreativeRole("fds", "Film distributor")

    /**
     * A person who, following the script and in creative cooperation with the Director, selects, arranges, and assembles the filmed material, controls the synchronization of picture and sound, and participates in other post-production tasks such as sound mixing and visual effects processing. Today, picture editing is often performed digitally.
     *
     * Also known as: *Motion Picture Editor*.
     */
    public object FilmEditor : DefaultCreativeRole("flm", "Film editor")

    /**
     * A producer responsible for most of the business aspects of a film.
     */
    public object FilmProducer : DefaultCreativeRole("fmp", "Film producer")

    /**
     * A person, family or organization responsible for creating an independent or personal film. A filmmaker is individually responsible for the conception and execution of all aspects of the film.
     */
    public object Filmmaker : DefaultCreativeRole("fmk", "Filmmaker")

    /**
     * A person or organization who is identified as the only party or the party of the first party. In the case of transfer of rights, this is the assignor, transferor, licensor, grantor, etc. Multiple parties can be named jointly as the first party.
     */
    public object FirstParty : DefaultCreativeRole("fpy", "First party")

    /**
     * A person or organization who makes or imitates something of value or importance, especially with the intent to defraud.
     *
     * Also known as: *Copier*, *Counterfeiter*.
     */
    public object Forger : DefaultCreativeRole("frg", "Forger")

    /**
     * A person, family, or organization formerly having legal possession of an item.
     */
    public object FormerOwner : DefaultCreativeRole("fmo", "Former owner")

    /**
     * A person or organization that furnished financial support for the production of the work.
     */
    public object Funder : DefaultCreativeRole("fnd", "Funder")

    /**
     * A person responsible for geographic information system *(GIS)* development and integration with global positioning system data.
     *
     * Also known as: *Geospatial Information Specialist*.
     */
    public object GeographicInformationSpecialist : DefaultCreativeRole("gis", "Geographic information specialist")

    /**
     * A person, family, or organization honored by a work or item *(e.g., the honoree of a festschrift, a person to whom a copy is presented)*.
     *
     * Also known as: *Honouree*, *Honouree Of Item*.
     */
    public object Honoree : DefaultCreativeRole("hnr", "Honoree")

    /**
     * A performer contributing to a resource by leading a program *(often broadcast)* that includes other guests, performers, etc. *(e.g., talk show host)*.
     */
    public object Host : DefaultCreativeRole("hst", "Host")

    /**
     * An organization hosting the event, exhibit, conference, etc., which gave rise to a resource, but having little or no responsibility for the content of the resource.
     */
    public object HostInstitution : DefaultCreativeRole("his", "Host institution")

    /**
     * A person providing decoration to a specific item using precious metals or color, often with elaborate designs and motifs.
     */
    public object Illuminator : DefaultCreativeRole("ilu", "Illuminator")

    /**
     * A person, family, or organization contributing to a resource by supplementing the primary content with drawings, diagrams, photographs, etc. If the work is primarily the artistic content created by this entity, use artist or photographer.
     */
    public object Illustrator : DefaultCreativeRole("ill", "Illustrator")

    /**
     * A person who has written a statement of dedication or gift.
     */
    public object Inscriber : DefaultCreativeRole("ins", "Inscriber")

    /**
     * A performer contributing to a resource by playing a musical instrument.
     */
    public object Instrumentalist : DefaultCreativeRole("itr", "Instrumentalist")

    /**
     * A person, family or organization responsible for creating or contributing to a resource by responding to an interviewer, usually a reporter, pollster, or some other information gathering agent.
     */
    public object Interviewee : DefaultCreativeRole("ive", "Interviewee")

    /**
     * A person, family, or organization responsible for creating or contributing to a resource by acting as an interviewer, reporter, pollster, or some other information gathering agent.
     */
    public object Interviewer : DefaultCreativeRole("ivr", "Interviewer")

    /**
     * A person, family, or organization responsible for creating a new device or process.
     *
     * Also known as: *Patent Inventor*.
     */
    public object Inventor : DefaultCreativeRole("inv", "Inventor")

    /**
     * A person, family or organization issuing a work, such as an official organ of the body.
     */
    public object IssuingBody : DefaultCreativeRole("isb", "Issuing body")

    /**
     * A person who hears and decides on legal matters in court.
     */
    public object Judge : DefaultCreativeRole("jud", "Judge")

    /**
     * A jurisdiction governed by a law, regulation, etc., that was enacted by another jurisdiction.
     */
    public object JurisdictionGoverned : DefaultCreativeRole("jug", "Jurisdiction governed")

    /**
     * An organization that provides scientific analyses of material samples.
     */
    public object Laboratory : DefaultCreativeRole("lbr", "Laboratory")

    /**
     * A person or organization that manages or supervises work done in a controlled setting or environment.
     *
     * Also known as: *Lab Director*.
     */
    public object LaboratoryDirector : DefaultCreativeRole("ldr", "Laboratory director")

    /**
     * An architect responsible for creating landscape works. This work involves coordinating the arrangement of existing and proposed land features and structures.
     */
    public object LandscapeArchitect : DefaultCreativeRole("lsa", "Landscape architect")

    /**
     * A person or organization that takes primary responsibility for a particular activity or endeavor. May be combined with another relator term or code to show the greater importance this person or organization has regarding that particular role. If more than one relator is assigned to a heading, use the Lead relator only if it applies to all the relators.
     */
    public object Lead : DefaultCreativeRole("led", "Lead")

    /**
     * A person or organization permitting the temporary use of a book, manuscript, etc., such as for photocopying or microfilming.
     */
    public object Lender : DefaultCreativeRole("len", "Lender")

    /**
     * A person or organization who files a libel in an ecclesiastical or admiralty case.
     */
    public object Libelant : DefaultCreativeRole("lil", "Libelant")

    /**
     * A libelant who takes an appeal from one ecclesiastical court or admiralty to another to reverse the judgment.
     */
    public object LibelantAppellant : DefaultCreativeRole("lit", "Libelant-appellant")

    /**
     * A libelant against whom an appeal is taken from one ecclesiastical court or admiralty to another to reverse the judgment.
     */
    public object LibelantAppellee : DefaultCreativeRole("lie", "Libelant-appellee")

    /**
     * A person or organization against whom a libel has been filed in an ecclesiastical court or admiralty.
     */
    public object Libelee : DefaultCreativeRole("lel", "Libelee")

    /**
     * A libelee who takes an appeal from one ecclesiastical court or admiralty to another to reverse the judgment.
     */
    public object LibeleeAppellant : DefaultCreativeRole("let", "Libelee-appellant")

    /**
     * A libelee against whom an appeal is taken from one ecclesiastical court or admiralty to another to reverse the judgment.
     */
    public object LibeleeAppellee : DefaultCreativeRole("lee", "Libelee-appellee")

    /**
     * An author of a libretto of an opera or other stage work, or an oratorio.
     */
    public object Librettist : DefaultCreativeRole("lbt", "Librettist")

    /**
     * A person or organization who is an original recipient of the right to print or publish.
     */
    public object Licensee : DefaultCreativeRole("lse", "Licensee")

    /**
     * A person or organization who is a signer of the license, imprimatur, etc.
     *
     * Also known as: *Imprimatur*.
     */
    public object Licensor : DefaultCreativeRole("lso", "Licensor")

    /**
     * A person or organization who designs the lighting scheme for a theatrical presentation, entertainment, motion picture, etc.
     */
    public object LightingDesigner : DefaultCreativeRole("lgd", "Lighting designer")

    /**
     * A person or organization who prepares the stone or plate for lithographic printing, including a graphic artist creating a design directly on the surface from which printing will be done.
     */
    public object Lithographer : DefaultCreativeRole("ltg", "Lithographer")

    /**
     * An author of the words of a non-dramatic musical work *(e.g. the text of a song)*, except for oratorios.
     */
    public object Lyricist : DefaultCreativeRole("lyr", "Lyricist")

    /**
     * The place of manufacture *(e.g., printing, duplicating, casting, etc.)* of a resource in a published form.
     */
    public object ManufacturePlace : DefaultCreativeRole("mfp", "Manufacture place")

    /**
     * A person or organization responsible for printing, duplicating, casting, etc. a resource.
     */
    public object Manufacturer : DefaultCreativeRole("mfr", "Manufacturer")

    /**
     * The entity responsible for marbling paper, cloth, leather, etc. used in construction of a resource.
     */
    public object Marbler : DefaultCreativeRole("mrb", "Marbler")

    /**
     * A person or organization performing the coding of SGML, HTML, or XML markup of metadata, text, etc.
     *
     * Also known as: *Encoder*.
     */
    public object MarkupEditor : DefaultCreativeRole("mrk", "Markup editor")

    /**
     * A person held to be a channel of communication between the earthly world and a world of spirits.
     */
    public object Medium : DefaultCreativeRole("med", "Medium")

    /**
     * A person or organization primarily responsible for compiling and maintaining the original description of a metadata set *(e.g., geospatial metadata set)*.
     */
    public object MetadataContact : DefaultCreativeRole("mdc", "Metadata contact")

    /**
     * An engraver responsible for decorations, illustrations, letters, etc. cut on a metal surface for printing or decoration.
     */
    public object MetalEngraver : DefaultCreativeRole("mte", "Metal-engraver")

    /**
     * A person, family, or organization responsible for recording the minutes of a meeting.
     */
    public object MinuteTaker : DefaultCreativeRole("mtk", "Minute taker")

    /**
     * A performer contributing to a resource by leading a program *(often broadcast)* where topics are discussed, usually with participation of experts in fields related to the discussion.
     */
    public object Moderator : DefaultCreativeRole("mod", "Moderator")

    /**
     * A person or organization that supervises compliance with the contract and is responsible for the report and controls its distribution. Sometimes referred to as the grantee, or controlling agency.
     */
    public object Monitor : DefaultCreativeRole("mon", "Monitor")

    /**
     * A person who transcribes or copies musical notation.
     */
    public object MusicCopyist : DefaultCreativeRole("mcp", "Music copyist")

    /**
     * A person who coordinates the activities of the composer, the sound editor, and sound mixers for a moving image production or for a musical or dramatic presentation or entertainment.
     */
    public object MusicalDirector : DefaultCreativeRole("msd", "Musical director")

    /**
     * A person or organization who performs music or contributes to the musical content of a work when it is not possible or desirable to identify the function more precisely.
     */
    public object Musician : DefaultCreativeRole("mus", "Musician")

    /**
     * A performer contributing to a resource by reading or speaking in order to give an account of an act, occurrence, course of events, etc.
     */
    public object Narrator : DefaultCreativeRole("nrt", "Narrator")

    /**
     * A performer contributing to an expression of a work by appearing on screen in nonfiction moving image materials or introductions to fiction moving image materials to provide contextual or background information. Use when another term *(e.g., narrator, host)* is either not applicable or not desired.
     */
    public object OnscreenPresenter : DefaultCreativeRole("osp", "Onscreen presenter")

    /**
     * A person or organization responsible for opposing a thesis or dissertation.
     */
    public object Opponent : DefaultCreativeRole("opn", "Opponent")

    /**
     * A person, family, or organization organizing the exhibit, event, conference, etc., which gave rise to a resource.
     *
     * Also known as: *Organizer Of Meeting*.
     */
    public object Organizer : DefaultCreativeRole("orm", "Organizer")

    /**
     * A person or organization performing the work, i.e., the name of a person or organization associated with the intellectual content of the work. This category does not include the publisher or personal affiliation, or sponsor except where it is also the corporate author.
     */
    public object Originator : DefaultCreativeRole("org", "Originator")

    /**
     * A role that has no equivalent in the MARC list.
     */
    public object Other : DefaultCreativeRole("oth", "Other")

    /**
     * A person, family, or organization that currently owns an item or collection, i.e.has legal possession of a resource.
     *
     * Also known as: *Current Owner*.
     */
    public object Owner : DefaultCreativeRole("own", "Owner")

    /**
     * A performer contributing to a resource by participating in a program *(often broadcast)* where topics are discussed, usually with participation of experts in fields related to the discussion.
     */
    public object Panelist : DefaultCreativeRole("pan", "Panelist")

    /**
     * A person or organization responsible for the production of paper, usually from wood, cloth, or other fibrous material.
     */
    public object Papermaker : DefaultCreativeRole("ppm", "Papermaker")

    /**
     * A person or organization that applied for a patent.
     */
    public object PatentApplicant : DefaultCreativeRole("pta", "Patent applicant")

    /**
     * A person or organization that was granted the patent referred to by the item.
     *
     * Also known as: *Patentee*.
     */
    public object PatentHolder : DefaultCreativeRole("pth", "Patent holder")

    /**
     * A person or organization responsible for commissioning a work. Usually a patron uses his or her means or influence to support the work of artists, writers, etc. This includes those who commission and pay for individual works.
     */
    public object Patron : DefaultCreativeRole("pat", "Patron")

    /**
     * A person contributing to a resource by performing music, acting, dancing, speaking, etc., often in a musical or dramatic presentation, etc. If specific codes are used, [prf] is used for a person whose principal skill is not known or specified.
     */
    public object Performer : DefaultCreativeRole("prf", "Performer")

    /**
     * An organization *(usually a government agency)* that issues permits under which work is accomplished.
     */
    public object PermittingAgency : DefaultCreativeRole("pma", "Permitting agency")

    /**
     * A person, family, or organization responsible for creating a photographic work.
     */
    public object Photographer : DefaultCreativeRole("pht", "Photographer")

    /**
     * The place to which a resource is sent, for example, the place of the postal address of a letter.
     */
    public object PlaceOfAddress : DefaultCreativeRole("pad", "Place of address")

    /**
     * A person or organization who brings a suit in a civil proceeding.
     */
    public object Plaintiff : DefaultCreativeRole("ptf", "Plaintiff")

    /**
     * A plaintiff who takes an appeal from one court or jurisdiction to another to reverse the judgment, usually in a legal proceeding.
     */
    public object PlaintiffAppellant : DefaultCreativeRole("ptt", "Plaintiff-appellant")

    /**
     * A plaintiff against whom an appeal is taken from one court or jurisdiction to another to reverse the judgment, usually in a legal proceeding.
     */
    public object PlaintiffAppellee : DefaultCreativeRole("pte", "Plaintiff-appellee")

    /**
     * A person, family, or organization involved in manufacturing a manifestation by preparing plates used in the production of printed images and/or text.
     */
    public object Platemaker : DefaultCreativeRole("plt", "Platemaker")

    /**
     * A person who is the faculty moderator of an academic disputation, normally proposing a thesis and participating in the ensuing disputation.
     */
    public object Praeses : DefaultCreativeRole("pra", "Praeses")

    /**
     * A person or organization mentioned in an “X presents” credit for moving image materials and who is associated with production, finance, or distribution in some way. A vanity credit; in early years, normally the head of a studio.
     */
    public object Presenter : DefaultCreativeRole("pre", "Presenter")

    /**
     * A person, family, or organization involved in manufacturing a manifestation of printed text, notated music, etc., from type or plates, such as a book, newspaper, magazine, broadside, score, etc.
     */
    public object Printer : DefaultCreativeRole("prt", "Printer")

    /**
     * A person or organization who prints illustrations from plates.
     *
     * Also known as: *Plates, Printer Of*.
     */
    public object PrinterOfPlates : DefaultCreativeRole("pop", "Printer of plates")

    /**
     * A person or organization who makes a relief, intaglio, or planographic printing surface.
     */
    public object Printmaker : DefaultCreativeRole("prm", "Printmaker")

    /**
     * A person or organization primarily responsible for performing or initiating a process, such as is done with the collection of metadata sets.
     */
    public object ProcessContact : DefaultCreativeRole("prc", "Process contact")

    /**
     * A person, family, or organization responsible for most of the business aspects of a production for screen, audio recording, television, webcast, etc. The producer is generally responsible for fund raising, managing the production, hiring key personnel, arranging for distributors, etc.
     */
    public object Producer : DefaultCreativeRole("pro", "Producer")

    /**
     * An organization that is responsible for financial, technical, and organizational management of a production for stage, screen, audio recording, television, webcast, etc.
     */
    public object ProductionCompany : DefaultCreativeRole("prn", "Production company")

    /**
     * A person or organization responsible for designing the overall visual appearance of a moving image production.
     */
    public object ProductionDesigner : DefaultCreativeRole("prs", "Production designer")

    /**
     * A person responsible for all technical and business matters in a production.
     */
    public object ProductionManager : DefaultCreativeRole("pmn", "Production manager")

    /**
     * A person or organization associated with the production *(props, lighting, special effects, etc.)* of a musical or dramatic presentation or entertainment.
     */
    public object ProductionPersonnel : DefaultCreativeRole("prd", "Production personnel")

    /**
     * The place of production *(e.g., inscription, fabrication, construction, etc.)* of a resource in an unpublished form.
     */
    public object ProductionPlace : DefaultCreativeRole("prp", "Production place")

    /**
     * A person, family, or organization responsible for creating a computer program.
     */
    public object Programmer : DefaultCreativeRole("prg", "Programmer")

    /**
     * A person or organization with primary responsibility for all essential aspects of a project, has overall responsibility for managing projects, or provides overall direction to a project manager.
     */
    public object ProjectDirector : DefaultCreativeRole("pdr", "Project director")

    /**
     * A person who corrects printed matter. For manuscripts, use [Corrector].
     */
    public object Proofreader : DefaultCreativeRole("pfr", "Proofreader")

    /**
     * A person or organization who produces, publishes, manufactures, or distributes a resource if specific codes are not desired *(e.g. [mfr], [pbl])*.
     */
    public object Provider : DefaultCreativeRole("prv", "Provider")

    /**
     * The place where a resource is published.
     */
    public object PublicationPlace : DefaultCreativeRole("pup", "Publication place")

    /**
     * A person or organization responsible for publishing, releasing, or issuing a resource.
     */
    public object Publisher : DefaultCreativeRole("pbl", "Publisher")

    /**
     * A person or organization who presides over the elaboration of a collective work to ensure its coherence or continuity. This includes editors-in-chief, literary editors, editors of series, etc.
     */
    public object PublishingDirector : DefaultCreativeRole("pbd", "Publishing director")

    /**
     * A performer contributing to a resource by manipulating, controlling, or directing puppets or marionettes in a moving image production or a musical or dramatic presentation or entertainment.
     */
    public object Puppeteer : DefaultCreativeRole("ppt", "Puppeteer")

    /**
     * A director responsible for the general management and supervision of a radio program.
     */
    public object RadioDirector : DefaultCreativeRole("rdd", "Radio director")

    /**
     * A producer responsible for most of the business aspects of a radio program.
     */
    public object RadioProducer : DefaultCreativeRole("rpc", "Radio producer")

    /**
     * A person contributing to a resource by supervising the technical aspects of a sound or video recording session.
     */
    public object RecordingEngineer : DefaultCreativeRole("rce", "Recording engineer")

    /**
     * A person or organization who uses a recording device to capture sounds and/or video during a recording session, including field recordings of natural sounds, folkloric events, music, etc.
     */
    public object Recordist : DefaultCreativeRole("rcd", "Recordist")

    /**
     * A person or organization who writes or develops the framework for an item without being intellectually responsible for its content.
     */
    public object Redaktor : DefaultCreativeRole("red", "Redaktor")

    /**
     * A person or organization who prepares drawings of architectural designs *(i.e., renderings)* in accurate, representational perspective to show what the project will look like when completed.
     */
    public object Renderer : DefaultCreativeRole("ren", "Renderer")

    /**
     * A person or organization who writes or presents reports of news or current events on air or in print.
     */
    public object Reporter : DefaultCreativeRole("rpt", "Reporter")

    /**
     * An organization that hosts data or material culture objects and provides services to promote long term, consistent and shared use of those data or objects.
     */
    public object Repository : DefaultCreativeRole("rps", "Repository")

    /**
     * A person who directed or managed a research project.
     */
    public object ResearchTeamHead : DefaultCreativeRole("rth", "Research team head")

    /**
     * A person who participated in a research project but whose role did not involve direction or management of it.
     */
    public object ResearchTeamMember : DefaultCreativeRole("rtm", "Research team member")

    /**
     * A person or organization responsible for performing research.
     *
     * Also known as: *Performer Of Research*.
     */
    public object Researcher : DefaultCreativeRole("res", "Researcher")

    /**
     * A person or organization who makes an answer to the courts pursuant to an application for redress *(usually in an equity proceeding)* or a candidate for a degree who defends or opposes a thesis provided by the praeses in an academic disputation.
     */
    public object Respondent : DefaultCreativeRole("rsp", "Respondent")

    /**
     * A respondent who takes an appeal from one court or jurisdiction to another to reverse the judgment, usually in an equity proceeding.
     */
    public object RespondentAppellant : DefaultCreativeRole("rst", "Respondent-appellant")

    /**
     * A respondent against whom an appeal is taken from one court or jurisdiction to another to reverse the judgment, usually in an equity proceeding.
     */
    public object RespondentAppellee : DefaultCreativeRole("rse", "Respondent-appellee")

    /**
     * A person or organization legally responsible for the content of the published material.
     */
    public object ResponsibleParty : DefaultCreativeRole("rpy", "Responsible party")

    /**
     * A person or organization, other than the original choreographer or director, responsible for restaging a choreographic or dramatic work and who contributes minimal new content.
     */
    public object Restager : DefaultCreativeRole("rsg", "Restager")

    /**
     * A person, family, or organization responsible for the set of technical, editorial, and intellectual procedures aimed at compensating for the degradation of an item by bringing it back to a state as close as possible to its original condition.
     */
    public object Restorationist : DefaultCreativeRole("rsr", "Restorationist")

    /**
     * A person or organization responsible for the review of a book, motion picture, performance, etc.
     */
    public object Reviewer : DefaultCreativeRole("rev", "Reviewer")

    /**
     * A person or organization responsible for parts of a work, often headings or opening parts of a manuscript, that appear in a distinctive color, usually red.
     */
    public object Rubricator : DefaultCreativeRole("rbr", "Rubricator")

    /**
     * A person or organization who is the author of a motion picture screenplay, generally the person who wrote the scenarios for a motion picture during the silent era.
     */
    public object Scenarist : DefaultCreativeRole("sce", "Scenarist")

    /**
     * A person or organization who brings scientific, pedagogical, or historical competence to the conception and realization on a work, particularly in the case of audio-visual items.
     */
    public object ScientificAdvisor : DefaultCreativeRole("sad", "Scientific advisor")

    /**
     * An author of a screenplay, script, or scene.
     *
     * Also known as: *Author Of Screenplay, Etc *.
     */
    public object Screenwriter : DefaultCreativeRole("aus", "Screenwriter")

    /**
     * A person who is an amanuensis and for a writer of manuscripts proper. For a person who makes pen-facsimiles, use [Facsimilist].
     */
    public object Scribe : DefaultCreativeRole("scr", "Scribe")

    /**
     * An artist responsible for creating a three-dimensional work by modeling, carving, or similar technique.
     */
    public object Sculptor : DefaultCreativeRole("scl", "Sculptor")

    /**
     * A person or organization who is identified as the party of the second part. In the case of transfer of right, this is the assignee, transferee, licensee, grantee, etc. Multiple parties can be named jointly as the second party.
     */
    public object SecondParty : DefaultCreativeRole("spy", "Second party")

    /**
     * A person or organization who is a recorder, redactor, or other person responsible for expressing the views of a organization.
     */
    public object Secretary : DefaultCreativeRole("sec", "Secretary")

    /**
     * A former owner of an item who sold that item to another owner.
     */
    public object Seller : DefaultCreativeRole("sll", "Seller")

    /**
     * A person who translates the rough sketches of the art director into actual architectural structures for a theatrical presentation, entertainment, motion picture, etc. Set designers draw the detailed guides and specifications for building the set.
     */
    public object SetDesigner : DefaultCreativeRole("std", "Set designer")

    /**
     * An entity in which the activity or plot of a work takes place, e.g. a geographic place, a time period, a building, an event.
     */
    public object Setting : DefaultCreativeRole("stg", "Setting")

    /**
     * A person whose signature appears without a presentation or other statement indicative of provenance. When there is a presentation statement, use [Inscriber].
     */
    public object Signer : DefaultCreativeRole("sgn", "Signer")

    /**
     * A performer contributing to a resource by using his/her/their voice, with or without instrumental accompaniment, to produce music. A singer's performance may or may not include actual words.
     *
     * Also known as: *Vocalist*.
     */
    public object Singer : DefaultCreativeRole("sng", "Singer")

    /**
     * A person who produces and reproduces the sound score *(both live and recorded)*, the installation of microphones, the setting of sound levels, and the coordination of sources of sound for a production.
     */
    public object SoundDesigner : DefaultCreativeRole("sds", "Sound designer")

    /**
     * A performer contributing to a resource by speaking words, such as a lecture, speech, etc.
     */
    public object Speaker : DefaultCreativeRole("spk", "Speaker")

    /**
     * A person, family, or organization sponsoring some aspect of a resource, e.g., funding research, sponsoring an event.
     *
     * Also known as: *Sponsoring Body*.
     */
    public object Sponsor : DefaultCreativeRole("spn", "Sponsor")

    /**
     * A person or organization contributing to a stage resource through the overall management and supervision of a performance.
     */
    public object StageDirector : DefaultCreativeRole("sgd", "Stage director")

    /**
     * A person who is in charge of everything that occurs on a performance stage, and who acts as chief of all crews and assistant to a director during rehearsals.
     */
    public object StageManager : DefaultCreativeRole("stm", "Stage manager")

    /**
     * An organization responsible for the development or enforcement of a standard.
     */
    public object StandardsBody : DefaultCreativeRole("stn", "Standards body")

    /**
     * A person or organization who creates a new plate for printing by molding or copying another printing surface.
     */
    public object Stereotyper : DefaultCreativeRole("str", "Stereotyper")

    /**
     * A performer contributing to a resource by relaying a creator's original story with dramatic or theatrical interpretation.
     */
    public object Storyteller : DefaultCreativeRole("stl", "Storyteller")

    /**
     * A person or organization that supports *(by allocating facilities, staff, or other resources)* a project, program, meeting, event, data objects, material culture objects, or other entities capable of support.
     *
     * Also known as: *Host, Supporting*.
     */
    public object SupportingHost : DefaultCreativeRole("sht", "Supporting host")

    /**
     * A person, family, or organization contributing to a cartographic resource by providing measurements or dimensional relationships for the geographic area represented.
     */
    public object Surveyor : DefaultCreativeRole("srv", "Surveyor")

    /**
     * A performer contributing to a resource by giving instruction or providing a demonstration.
     *
     * Also known as: *Instructor*.
     */
    public object Teacher : DefaultCreativeRole("tch", "Teacher")

    /**
     * A person who is ultimately in charge of scenery, props, lights and sound for a production.
     */
    public object TechnicalDirector : DefaultCreativeRole("tcd", "Technical director")

    /**
     * A director responsible for the general management and supervision of a television program.
     */
    public object TelevisionDirector : DefaultCreativeRole("tld", "Television director")

    /**
     * A producer responsible for most of the business aspects of a television program.
     */
    public object TelevisionProducer : DefaultCreativeRole("tlp", "Television producer")

    /**
     * A person under whose supervision a degree candidate develops and presents a thesis, mémoire, or text of a dissertation.
     *
     * Also known as: *Promoter*.
     */
    public object ThesisAdvisor : DefaultCreativeRole("ths", "Thesis advisor")

    /**
     * A person, family, or organization contributing to a resource by changing it from one system of notation to another. For a work transcribed for a different instrument or performing group, see [Arranger]. For makers of pen-facsimiles, use [Facsimilist].
     */
    public object Transcriber : DefaultCreativeRole("trc", "Transcriber")

    /**
     * A person or organization who renders a text from one language into another, or from an older form of a language into the modern form.
     */
    public object Translator : DefaultCreativeRole("trl", "Translator")

    /**
     * A person or organization who designs the type face used in a particular item.
     *
     * Also known as: *Designer Of Type*.
     */
    public object TypeDesigner : DefaultCreativeRole("tyd", "Type designer")

    /**
     * A person or organization primarily responsible for choice and arrangement of type used in an item. If the typographer is also responsible for other aspects of the graphic design of a book *(e.g., [BookDesigner])*, codes for both functions may be needed.
     */
    public object Typographer : DefaultCreativeRole("tyg", "Typographer")

    /**
     * A place where a university that is associated with a resource is located, for example, a university where an academic dissertation or thesis was presented.
     */
    public object UniversityPlace : DefaultCreativeRole("uvp", "University place")

    /**
     * A person in charge of a video production, e.g. the video recording of a stage production as opposed to a commercial motion picture. The videographer may be the camera operator or may supervise one or more camera operators. Do not confuse with cinematographer.
     */
    public object Videographer : DefaultCreativeRole("vdg", "Videographer")

    /**
     * An actor contributing to a resource by providing the voice for characters in radio and audio productions and for animated characters in moving image works, as well as by providing voice overs in radio and television commercials, dubbed resources, etc.
     */
    public object VoiceActor : DefaultCreativeRole("vac", "Voice actor")

    /**
     * Use for a person who verifies the truthfulness of an event or action.
     *
     * Also known as: *Deponent*, *Eyewitness*, *Observer*, *Onlooker*, *Testifier*.
     */
    public object Witness : DefaultCreativeRole("wit", "Witness")

    /**
     * A person or organization who makes prints by cutting the image in relief on the end-grain of a wood block.
     */
    public object WoodEngraver : DefaultCreativeRole("wde", "Wood engraver")

    /**
     * A person or organization who makes prints by cutting the image in relief on the plank side of a wood block.
     */
    public object Woodcutter : DefaultCreativeRole("wdc", "Woodcutter")

    /**
     * A person or organization who writes significant material which accompanies a sound recording or other audiovisual material.
     */
    public object WriterOfAccompanyingMaterial : DefaultCreativeRole("wam", "Writer of accompanying material")

    /**
     * A person, family, or organization contributing to an expression of a work by providing an interpretation or critical explanation of the original work.
     */
    public object WriterOfAddedCommentary : DefaultCreativeRole("wac", "Writer of added commentary")

    /**
     * A writer of words added to an expression of a musical work. For lyric writing in collaboration with a composer to form an original work, see lyricist.
     */
    public object WriterOfAddedLyrics : DefaultCreativeRole("wal", "Writer of added lyrics")

    /**
     * A person, family, or organization contributing to a non-textual resource by providing text for the non-textual work *(e.g., writing captions for photographs, descriptions of maps)*.
     */
    public object WriterOfAddedText : DefaultCreativeRole("wat", "Writer of added text")

    /**
     * A person, family, or organization contributing to a resource by providing an introduction to the original work.
     */
    public object WriterOfIntroduction : DefaultCreativeRole("win", "Writer of introduction")

    /**
     * A person, family, or organization contributing to a resource by providing a preface to the original work.
     */
    public object WriterOfPreface : DefaultCreativeRole("wpr", "Writer of preface")

    /**
     * A person, family, or organization contributing to a resource by providing supplementary textual content *(e.g., an introduction, a preface)* to the original work.
     */
    public object WriterOfSupplementaryTextualContent :
        DefaultCreativeRole("wst", "Writer of supplementary textual content")
}