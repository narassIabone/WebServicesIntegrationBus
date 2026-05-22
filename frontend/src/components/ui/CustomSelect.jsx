import React, { useState, useRef, useEffect } from 'react';
import { ChevronDown, Check } from 'lucide-react';

const CustomSelect = ({ value, onChange, options, placeholder, icon: Icon, labelTransformer }) => {
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef(null);

    // Закрытие при клике вне компонента
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleSelect = (option) => {
        onChange(option);
        setIsOpen(false);
    };

    return (
        <div className="relative w-full" ref={dropdownRef}>
            <button
                type="button"
                onClick={() => setIsOpen(!isOpen)}
                className="w-full flex items-center gap-3 pl-10 pr-4 py-2.5 bg-slate-50 border-none rounded-xl text-sm font-bold text-gray-600 transition-all hover:bg-slate-100 focus:ring-2 focus:ring-blue-500/20 text-left"
            >
                {Icon && <Icon className="absolute left-3 text-gray-400" size={18} />}

                <span className="truncate">
                    {value === 'ALL' ? (placeholder || 'Все') : (labelTransformer ? labelTransformer(value) : value)}
                </span>

                <ChevronDown
                    size={16}
                    className={`ml-auto text-gray-400 transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`}
                />
            </button>

            {isOpen && (
                <div className="absolute z-50 w-full mt-2 bg-white rounded-2xl shadow-xl border border-gray-100 py-2 max-h-60 overflow-y-auto animate-in fade-in zoom-in duration-150">
                    {options.map((option) => (
                        <button
                            key={option}
                            onClick={() => handleSelect(option)}
                            className={`w-full flex items-center justify-between px-4 py-2 text-sm transition-colors
                                ${value === option
                                ? 'bg-blue-50 text-blue-600 font-bold'
                                : 'text-gray-600 hover:bg-slate-50'}`}
                        >
                            <span className="truncate">
                                {option === 'ALL'
                                    ? (placeholder || 'Все')
                                    : (labelTransformer ? labelTransformer(option) : option)
                                }
                            </span>
                            {value === option && <Check size={14} className="text-blue-600" />}
                        </button>
                    ))}
                </div>
            )}
        </div>
    );
};

export default CustomSelect;