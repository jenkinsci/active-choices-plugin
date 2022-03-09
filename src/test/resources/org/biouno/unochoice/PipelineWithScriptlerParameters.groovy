properties([
    parameters([
        [
            $class: 'ChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: 'param001 description',
            filterLength: 1,
            filterable: true,
            name: 'param001',
            randomName: 'random-name',
            script: [
                $class: 'ScriptlerScript',
                scriptlerScriptId: 'dummy.groovy',
                parameters: [
                    [name: 'arg1', value: 'bla']
                ]
            ]
        ],
        [
            $class: 'CascadeChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: 'param002 description',
            filterLength: 1,
            filterable: true,
            name: 'param002',
            randomName: 'random-name',
            referencedParameters: 'param001',
            script: [
                $class: 'GroovyScript',
                script: [
                    $class: 'SecureGroovyScript',
                    script: 'return [PARAM001]',
                    sandbox: false
                ],
                fallbackScript: [
                    $class: 'SecureGroovyScript',
                    script: 'return []',
                    sandbox: false
                ]
            ]
        ]
    ])
])
