import React, { useState, useEffect, useCallback, useRef } from 'react';
import { ChevronDown, Plus, ArrowRight, X,  GripHorizontal } from 'lucide-react';
import { NODE_METADATA } from '../../constants/nodeMetadata.js';

const ConfigSidebar = ({
                           routeName, setRouteName, selectedNode, selectedLink,
                           updateNodeConfig, addMappingRow, updateMappingRow, removeMappingRow, fullConfigJson
                       }) => {
    // Стейт для высоты блока JSON
    const [jsonHeight, setJsonHeight] = useState(300);
    const isResizing = useRef(false);
    const resizeRef = useRef({ startY: 0, startHeight: 0 });
    const [isOpenField, setIsOpenField] = useState(null);

    // Логика изменения размера
    const startResizing = useCallback((e) => {
        isResizing.current = true;
        // ЗАПОМИНАЕМ: начальную точку Y и текущую высоту блока
        resizeRef.current = {
            startY: e.clientY,
            startHeight: jsonHeight
        };
        document.body.style.cursor = 'ns-resize';
        // Запрещаем выделение текста во время перетаскивания
        document.body.style.userSelect = 'none';
    }, [jsonHeight]);

    const resize = useCallback((e) => {
        if (!isResizing.current) return;

        // ВЫЧИСЛЯЕМ: разницу между точкой старта и текущим положением
        const delta = resizeRef.current.startY - e.clientY;
        const newHeight = resizeRef.current.startHeight + delta;

        // Ограничители (чтобы не схлопнуть и не растянуть на весь экран)
        if (newHeight > 100 && newHeight < window.innerHeight * 0.8) {
            setJsonHeight(newHeight);
        }
    }, []);

    const stopResizing = useCallback(() => {
        isResizing.current = false;
        document.body.style.cursor = 'default';
        document.body.style.userSelect = 'auto';
    }, []);

    useEffect(() => {
        window.addEventListener('mousemove', resize);
        window.addEventListener('mouseup', stopResizing);
        return () => {
            window.removeEventListener('mousemove', resize);
            window.removeEventListener('mouseup', stopResizing);
        };
    }, [resize, stopResizing]);

    // ФИКС JSON: Превращаем строку обратно в объект для красивого форматирования
    const getCleanJson = () => {
        try {
            return typeof fullConfigJson === 'string' ? JSON.parse(fullConfigJson) : fullConfigJson;
        } catch (e) {
            return { error: "Invalid JSON format" };
        }
    };

    const inputClass = "w-full px-3 py-1.5 border-2 border-slate-200 rounded-lg text-xs font-bold outline-none bg-white focus:border-blue-400 transition-all placeholder:text-slate-200 placeholder:font-medium";
    const selectClass = "w-full px-3 py-1.5 border-2 border-slate-200 rounded-lg text-xs font-bold outline-none bg-white focus:border-blue-400 appearance-none cursor-pointer pr-8 text-slate-700 shadow-sm";

    const renderSingleField = (field, config) => (
        <div key={field.name} className="bg-slate-50 p-2 rounded-xl border border-slate-100">
            <label className="block font-black uppercase text-[10px] text-slate-400 mb-1 ml-1">
                {field.label} {field.required && <span className="text-red-400">*</span>}
            </label>
            {renderInput(field, config)}
        </div>
    );

    const renderGroup = (groupFields, config) => (
        <div key={`group-${groupFields[0].name}`}
             className="ml-4 bg-blue-50/20 border-l-2 border-blue-400/40 rounded-r-xl overflow-hidden shadow-sm animate-in fade-in slide-in-from-left-1">
            <div className="p-3 space-y-3">
                <div className="text-[9px] font-black text-blue-500/60 uppercase tracking-widest flex items-center gap-2 mb-1">
                    <div className="w-1 h-1 rounded-full bg-blue-400"></div>
                    Sub-Configuration
                </div>
                {groupFields.map(f => (
                    <div key={f.name}>
                        <label className="block font-black uppercase text-[8px] text-blue-400/80 mb-1 ml-1">
                            {f.label}
                        </label>
                        {renderInput(f, config)}
                    </div>
                ))}
            </div>
        </div>
    );

    const renderInput = (field, config) => {
        const handleInteraction = (e) => e.stopPropagation();
        const displayValue = config[field.name] ?? field.default ?? '';

        if (field.type === 'select') {
            return (
                <div className="relative" onMouseDown={handleInteraction}>
                    {/* Имитация твоего инпута */}
                    <button
                        onClick={() => setIsOpenField(isOpenField === field.name ? null : field.name)}
                        className={`${selectClass} flex items-center justify-between !pr-3`}
                    >
                        <span className="truncate">{displayValue}</span>
                        <ChevronDown size={12} className={`text-slate-400 transition-transform ${isOpenField === field.name ? 'rotate-180' : ''}`} />
                    </button>

                    {/* Компактный список в стиле твоего конфига */}
                    {isOpenField === field.name && (
                        <div className="absolute z-[60] w-full mt-1 bg-white border-2 border-slate-200 rounded-xl shadow-lg overflow-hidden animate-in fade-in zoom-in-95 duration-100">
                            {field.options.map(opt => (
                                <button
                                    key={opt}
                                    onClick={() => {
                                        updateNodeConfig(field.name, opt);
                                        setIsOpenField(null);
                                    }}
                                    className={`w-full px-3 py-2 text-[11px] font-bold text-left transition-colors
                                ${displayValue === opt
                                        ? 'bg-blue-50 text-blue-600'
                                        : 'text-slate-600 hover:bg-slate-50'}`}
                                >
                                    {opt}
                                </button>
                            ))}
                        </div>
                    )}
                </div>
            );
        }
        return (
            <input
                type={field.type}
                placeholder={field.placeholder}
                value={displayValue}
                onMouseDown={handleInteraction}
                onChange={(e) => updateNodeConfig(field.name, field.type === 'number' ? parseInt(e.target.value) : e.target.value)}
                className={inputClass}
            />
        );
    };

    return (
        <div className="w-96 bg-white border-l border-slate-200 flex flex-col h-full shadow-2xl relative z-20 font-sans overflow-hidden">
            {/* Header */}
            <div className="p-6 border-b border-slate-100 bg-white/80 backdrop-blur-md shrink-0">
                <h2 className="text-xl font-black text-slate-800 tracking-tight mb-6">ESB CONFIGURATOR</h2>
                <div>
                    <label className="block text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Route Name</label>
                    <input
                        type="text"
                        value={routeName}
                        onChange={(e) => setRouteName(e.target.value)}
                        className="w-full px-4 py-2 bg-slate-50 border-2 border-slate-100 rounded-xl text-sm font-bold text-slate-700 outline-none focus:border-blue-500 focus:bg-white transition-all shadow-inner"
                        placeholder="Enter route name..."
                    />
                </div>
            </div>

            {/* Content Area */}
            <div className="flex-1 overflow-y-auto p-6 space-y-6 custom-scrollbar">
                {selectedNode ? (
                    <div className="space-y-4 animate-in fade-in zoom-in-95 duration-200">
                        <div className="bg-slate-900 rounded-2xl p-4 shadow-xl relative overflow-hidden group shrink-0">
                            <div className="relative z-10 flex items-center gap-4">
                                <div className="text-3xl font-black text-white/20">#{selectedNode.id}</div>
                                <div>
                                    <div className="text-[10px] font-black text-blue-400 uppercase tracking-widest mb-0.5">{selectedNode.data.type}</div>
                                    <div className="text-lg font-bold text-white leading-none">{selectedNode.data.label}</div>
                                </div>
                            </div>
                        </div>
                        <div className="p-3 bg-slate-50 rounded-xl border border-slate-200">
                            <label className="block text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1.5 ml-1">
                                Название узла
                            </label>
                            <input
                                type="text"
                                value={selectedNode.data.config?.name || ''}
                                onChange={(e) => updateNodeConfig('name', e.target.value)}
                                className={inputClass}
                                placeholder="Например: Проверка паспорта, Split-1..."
                            />
                        </div>
                        <div className="flex items-center justify-between p-3 bg-slate-50 rounded-xl border border-slate-200 mb-4">
                            <div className="flex flex-col">
                                <span className="text-[11px] font-bold text-slate-700 uppercase">Стартовый узел</span>
                                <span className="text-[9px] text-slate-400">С этой ноды начнется выполнение маршрута</span>
                            </div>
                            <button
                                onClick={() => updateNodeConfig('isStart', !selectedNode.data.config.isStart)}
                                className={`w-10 h-5 rounded-full transition-colors relative ${selectedNode.data.config.isStart ? 'bg-emerald-500' : 'bg-slate-300'}`}
                            >
                                <div className={`absolute top-1 w-3 h-3 bg-white rounded-full transition-transform ${selectedNode.data.config.isStart ? 'left-6' : 'left-1'}`} />
                            </button>
                        </div>
                        <div className="flex flex-col gap-2">
                            {(() => {
                                const fields = NODE_METADATA[selectedNode.data.type]?.fields || [];
                                const config = selectedNode.data.config || {};
                                const renderedElements = [];
                                let currentGroup = [];
                                fields.forEach((field, index) => {
                                    const isVisible = !field.showIf || field.showIf(config);
                                    if (!isVisible) return;
                                    if (field.showIf) {
                                        currentGroup.push(field);
                                    } else {
                                        if (currentGroup.length > 0) {
                                            renderedElements.push(renderGroup(currentGroup, config));
                                            currentGroup = [];
                                        }
                                        renderedElements.push(renderSingleField(field, config));
                                    }
                                    if (index === fields.length - 1 && currentGroup.length > 0) {
                                        renderedElements.push(renderGroup(currentGroup, config));
                                    }
                                });
                                return renderedElements;
                            })()}
                        </div>
                    </div>
                ) : selectedLink ? (
                    <div className="space-y-6 animate-in fade-in slide-in-from-right-4 duration-200 overflow-x-hidden">
                        <div className="bg-amber-50 border border-amber-100 rounded-2xl p-5 shrink-0">
                            <div className="text-[10px] font-black text-amber-600 uppercase tracking-widest mb-3">Link Mapping</div>
                            <div className="flex items-center justify-between bg-white rounded-xl p-3 border border-amber-200/50 shadow-sm">
                                <div className="flex items-center gap-2">
                                    <div className="w-1.5 h-1.5 rounded-full bg-amber-400" />
                                    <span className="text-[10px] font-bold text-slate-600 uppercase">Node {selectedLink.source}</span>
                                </div>
                                <ArrowRight size={14} className="text-amber-400" />
                                <div className="flex items-center gap-2">
                                    <span className="text-[10px] font-bold text-slate-600 uppercase">Node {selectedLink.target}</span>
                                    <div className="w-1.5 h-1.5 rounded-full bg-amber-400" />
                                </div>
                            </div>
                        </div>

                        <div className="space-y-2">
                            {(selectedLink.data?.fieldMapping || []).map((map, idx) => (
                                <div key={idx} className="flex items-center gap-1.5 group animate-in fade-in slide-in-from-top-1">
                                    <input
                                        placeholder="Src"
                                        value={map.sourceField}
                                        onChange={(e) => updateMappingRow(idx, 'sourceField', e.target.value)}
                                        className="w-[42%] px-2 py-1.5 bg-white border-2 border-slate-100 rounded-lg text-[11px] font-bold focus:border-amber-400 outline-none transition-all"
                                    />
                                    <ArrowRight size={10} className="text-slate-300 shrink-0" />
                                    <input
                                        placeholder="Trgt"
                                        value={map.targetField}
                                        onChange={(e) => updateMappingRow(idx, 'targetField', e.target.value)}
                                        className="w-[42%] px-2 py-1.5 bg-white border-2 border-slate-100 rounded-lg text-[11px] font-bold focus:border-amber-400 outline-none transition-all"
                                    />
                                    <button
                                        onClick={() => removeMappingRow(idx)}
                                        className="p-1 text-slate-300 hover:text-red-500 transition-all shrink-0"
                                    >
                                        <X size={14} />
                                    </button>
                                </div>
                            ))}
                            <button
                                onClick={addMappingRow}
                                className="w-full py-2 border-2 border-dashed border-slate-200 rounded-xl text-slate-400 text-[10px] font-black uppercase hover:border-amber-400 hover:text-amber-500 transition-all flex items-center justify-center gap-2"
                            >
                                <Plus size={12} /> Add Field
                            </button>
                        </div>
                    </div>
                ) : (
                    <div className="h-full flex flex-col items-center justify-center text-center opacity-30">
                        <ArrowRight size={32} className="mb-4 text-slate-300" />
                        <p className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Select a node or link</p>
                    </div>
                )}
            </div>

            {/* JSON Output */}
            <div
                style={{ height: `${jsonHeight}px` }}
                className="bg-slate-900 border-t border-white/10 relative shadow-inner flex flex-col shrink-0"
            >
                {/* Ресайзер */}
                <div
                    onMouseDown={startResizing}
                    className="absolute -top-1 left-0 right-0 h-2 cursor-ns-resize bg-transparent hover:bg-blue-500/30 transition-colors z-30"
                />

                {/* Декоративная линия ресайзера */}
                <div className="w-full h-[1px] bg-white/5 relative shrink-0">
                    <div className="absolute left-1/2 -translate-x-1/2 -top-2 px-3 bg-slate-900 border border-white/10 rounded-full text-slate-600">
                        <GripHorizontal size={12} />
                    </div>
                </div>

                <div className="p-6 h-full flex flex-col min-h-0">
                    <div className="flex justify-between items-center mb-4 shrink-0">
                        <div className="flex items-center gap-2">
                            <div className="w-2 h-2 bg-emerald-500 rounded-full animate-pulse shadow-[0_0_8px_rgba(16,185,129,0.6)]"></div>
                            <span className="text-[9px] font-black text-slate-500 uppercase tracking-widest">Live Config JSON</span>
                        </div>
                    </div>

                    {/* ФИКС ВИДА: Используем JSON.stringify для форматирования */}
                    <div className="flex-1 bg-black/20 rounded-xl border border-white/5 overflow-hidden">
                        <pre className="h-full p-4 text-[11px] text-emerald-400/90 font-mono overflow-y-auto custom-scrollbar whitespace-pre-wrap break-all">
                            {JSON.stringify(getCleanJson(), null, 2)}
                        </pre>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ConfigSidebar;