import {Split, Webhook, Merge, Filter } from 'lucide-react';

export const NODE_METADATA = {
    API_CALL: {
        title: 'API Call',
        description: 'REST/SOAP Gateway',
        icon: Webhook,
        color: 'text-amber-500',
        bgColor: 'bg-amber-50',
        fields: [
            { name: 'url', label: 'URL Address', type: 'text', required: true, placeholder: 'https://api.example.com/{id}' },
            { name: 'method', label: 'HTTP Method', type: 'select', options: ['GET', 'POST', 'PUT', 'DELETE'], default: 'POST' },
            { name: 'timeout', label: 'Timeout (ms)', type: 'number', default: 5000 },
            { name: 'protocol', label: 'Protocol', type: 'select', options: ['REST', 'SOAP', 'RAW'], default: 'REST' },

            { name: 'content_type', label: 'Content Type', type: 'text/plain', default: 'text/plain', showIf: (c) => c.protocol === 'RAW' },

            { name: 'soap_root_element', label: 'SOAP Root Element', type: 'text', placeholder: 'getLoanRequest', showIf: (c) => c.protocol === 'SOAP' },
            { name: 'soap_namespace', label: 'SOAP Namespace', type: 'text', placeholder: 'http://example.org/loans', showIf: (c) => c.protocol === 'SOAP' },
            { name: 'soap_prefix', label: 'SOAP Prefix', type: 'text', placeholder: 'loan', showIf: (c) => c.protocol === 'SOAP' },
            { name: 'soap_action', label: 'SOAP Action', type: 'text', showIf: (c) => c.protocol === 'SOAP' },
            { name: 'is_namespace_aware', label: 'Namespace Aware', type: 'select', options: ['true', 'false'], default: 'false', showIf: (c) => c.protocol === 'SOAP' },
            { name: 'target_field', label: 'Target Field(s)', type: 'text', placeholder: 'e.g. passport or fio,birthday' },

            { name: 'custom_headers', label: 'Custom Headers', type: 'text', placeholder: 'Key1=Value1,Key2=Value2' },
            { name: 'auth_type', label: 'Auth Type', type: 'select', options: ['NONE', 'BEARER', 'API_KEY', 'BASIC'], default: 'NONE' },

            { name: 'auth_token', label: 'JWT Token', type: 'text', placeholder: '{jwt_token}', showIf: (c) => c.auth_type === 'BEARER' },

            { name: 'api_key_name', label: 'API Key Name', type: 'text', default: 'X-API-Key', showIf: (c) => c.auth_type === 'API_KEY' },
            { name: 'api_key_value', label: 'API Key Value', type: 'text', showIf: (c) => c.auth_type === 'API_KEY' },

            { name: 'auth_user', label: 'Username', type: 'text', showIf: (c) => c.auth_type === 'BASIC' },
            { name: 'auth_password', label: 'Password', type: 'text', showIf: (c) => c.auth_type === 'BASIC' },


            { name: 'response_strategy', label: 'Response Strategy', type: 'select', options: ['OVERRIDE', 'MERGE_FULL', 'MERGE_SELECTIVE'], default: 'OVERRIDE' },
            { name: 'response_mapping', label: 'Response Mapping', type: 'text', placeholder: 'contract=Body.getResponse.id', showIf: (c) => c.response_strategy === 'MERGE_SELECTIVE' }
        ]
    },
    SPLITTER: {
        title: 'Splitter Node',
        description: 'Logic Split',
        icon: Split,
        color: 'text-purple-500',
        bgColor: 'text-purple-500',
        fields: [
            { name: 'splitCount', label: 'Split Count', type: 'number', required: true }
        ]
    },
    AGGREGATOR: {
        title: 'Aggregator',
        description: 'Wait for N fragments to merge',
        icon: Merge,
        color: 'text-blue-600',
        bgColor: 'bg-blue-50',
        fields: [
            { name: 'expected_count', label: 'Expected Fragments Count', type: 'number', default: 2, required: true, placeholder: 'Enter number of fragments...'}
        ]
    },
    FILTER: {
        title: 'Filter',
        description: 'SpEL logical branching',
        icon: Filter,
        color: 'text-rose-500',
        bgColor: 'bg-rose-50',
        accentColor: 'text-rose-700',
        fields: [
            { name: 'expression', label: 'SpEL Expression', type: 'text', required: true, placeholder: "#payload['amount'] > 100" },
            { name: 'ifFalse', label: 'If Condition is False', type: 'select', options: ['CONTINUE', 'DIVERGE', 'FINISH'], default: 'CONTINUE' },

            { name: 'on_false_target_node', label: 'False Target Node ID', type: 'text', placeholder: 'e.g. 5', showIf: (c) => c.ifFalse === 'DIVERGE' }
        ]
    },
};