import React from 'react';
import { Copy } from 'lucide-react';

const JsonBlock = ({ data, title, icon: Icon, height = "h-full" }) => {
    let displayData = data;
    if (typeof data === 'string') {
        try { displayData = JSON.parse(data); } catch { displayData = data; }
    }

    const copyToClipboard = () => {
        const text = typeof displayData === 'object' ? JSON.stringify(displayData, null, 2) : displayData;
        navigator.clipboard.writeText(text);
    };

    return (
        <div className={`flex flex-col ${height}`}>
            <div className="flex items-center justify-between mb-1.5 px-1">
                <div className="flex items-center gap-1.5">
                    {Icon && <Icon size={12} className="text-gray-400" />}
                    <span className="text-[10px] font-bold text-gray-400 uppercase tracking-tight">{title}</span>
                </div>
                <button onClick={copyToClipboard} className="text-gray-400 hover:text-blue-500 transition-colors p-1">
                    <Copy size={12} />
                </button>
            </div>
            <pre className="bg-slate-900 text-blue-300 p-3 rounded-xl text-[11px] overflow-auto font-mono flex-1 border border-slate-800
                           scrollbar-thin scrollbar-thumb-slate-700 scrollbar-track-transparent whitespace-pre-wrap break-all">
                {displayData ? (typeof displayData === 'object' ? JSON.stringify(displayData, null, 2) : displayData) : "// Данные отсутствуют"}
            </pre>
        </div>
    );
};

export default JsonBlock;