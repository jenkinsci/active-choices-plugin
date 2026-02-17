buildPlugin(
    useContainerAgent: true,
    failFast: false,
    forkCount: '1C',
    configurations: [
        [platform: 'linux', jdk: 21],
        [platform: 'linux', jdk: 25], // Fails TestRevertingAsynchronousProxy if first in list
        // [platform: 'windows', jdk: 17],
    ]
)
