const buildConfluentArchive = () => {
  const steps = [
    'cd connect-extension',
    'sed -ie \'s/"version": .*/"version": "${nextRelease.version}"/g\' confluentArchiveBase/manifest.json',
    'mvn -q versions:set -DnewVersion=${nextRelease.version}',
    'mvn clean install -Dmaven.test.skip=true',
  ]

  return `(${steps.join(' && ')})`
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
