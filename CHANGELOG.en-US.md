# Changelog
All notable changes about knowledgebase-service will be documented in this file.

## [0.19.0] - 2019-10-18

### Added

- Support documents move: move a single document and move the parent document
- Supporting documentation time is saved as a draft, abnormal exit can be restored
- Supporting documentation sets the default personal edit mode
- Support document full-text retrieval, according to the keywords weight to return the result list
- Project layer can view the organization of the document
- Documentation to support full screen viewing and editing
- Document version record increase header information
- Home page increased view recently updated list of documents

### Changed

- Home page increased view recently updated list of documents
- Document loading performance optimization

### Fixed

- Fix dirty data for deleted documents

## [0.18.0] - 2019-06-21

### Added

- Support version rollback and version comparison.
- Wiki articles migrate to knowledge management.
- Documents can share by link.
- Support word import and support preview after import.
- Page support export pdf.

### Changed

- Adjust API permissions of delete space, page and comment.
- Optimizing the Preservation of Articles.
- Optimization of Editing Processing.

### Fixed

- Fix a bug that the article name will change to the last article name when saving the article.
  

## [0.17.0] - 2019-05-24

### Added

- Added the menu of `knowledge management'for organization and project level.
- Added quick creation,edit and delete documents.
- Documentation supports `Markdown'and `WYSIWYG' editing styles.
- Documents are displayed in a tree structure and sorted directly by dragging.
- Documents can upload and download attachment, comment, log view.
- Added document directory structure for viewing.
