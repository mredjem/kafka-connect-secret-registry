const generateShaSignatures = () => {
  const steps = [
    'cd connect-extension/target',
    'for f in *.{jar,zip}; do sha512sum $f > $f.sha512; done',
  ]

  return `(${steps.join(' && ')})`
}

const buildConfluentArchive = () => {
  const steps = [
    'cd connect-extension',
    'sed -ie \'s/"version": .*/"version": "${nextRelease.version}"/g\' confluentArchiveBase/manifest.json',
    'mvn -q versions:set -DnewVersion=${nextRelease.version}',
    'mvn clean install',
  ]

  return `(${steps.join(' && ')}) && ${generateShaSignatures()}`
}

module.exports = {
  branches: [
    'main',
  ],
  repositoryUrl: 'git@github.com:mredjem/kafka-connect-secret-registry.git',
  tagFormat: '${version}',
  plugins: [
    [
      '@semantic-release/commit-analyzer',
      {
        preset: 'angular',
      }
    ],
    '@semantic-release/release-notes-generator',
    '@semantic-release/changelog',
    [
      '@semantic-release/exec',
      {
        prepareCmd: buildConfluentArchive(),
      }
    ],
    [
      '@semantic-release/github',
      {
        assets: [
          'connect-extension/target/*.jar',
          'connect-extension/target/*.zip',
          'connect-extension/target/*.sha512',
        ]
      }
    ],
    [
      "@semantic-release/git",
      {
        assets: [
          'CHANGELOG.md',
          'connect-extension/pom.xml',
          'connect-extension/confluentArchiveBase/manifest.json',
        ],
        message: 'chore(release): ${nextRelease.version} [skip ci]\n\n${nextRelease.notes}',
      }
    ]
  ]
}
