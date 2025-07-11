document.addEventListener('DOMContentLoaded', function() {
    const board = document.getElementById('scrabbleBoard');
    const generateBtn = document.getElementById('generatePuzzle');
    const showSolutionBtn = document.getElementById('showSolution');
    const remainingTilesDiv = document.getElementById('remainingTiles');
    const placedCountSpan = document.getElementById('placedCount');
    const remainingCountSpan = document.getElementById('remainingCount');
    
    let currentPuzzle = null;
    let isGenerating = false;
    
    // Define special cell positions (0-indexed)
    const specialCells = {
        // Triple Word (corners and center edges)
        'triple-word': [
            [0, 0], [0, 7], [0, 14],
            [7, 0], [7, 14],
            [14, 0], [14, 7], [14, 14]
        ],
        
        // Double Word (diagonal lines from corners)
        'double-word': [
            [1, 1], [2, 2], [3, 3], [4, 4], [5, 5], [6, 6], [7, 7], [8, 8], [9, 9], [10, 10], [11, 11], [12, 12], [13, 13],
            [1, 13], [2, 12], [3, 11], [4, 10], [5, 9], [6, 8], [8, 6], [9, 5], [10, 4], [11, 3], [12, 2], [13, 1]
        ],
        
        // Triple Letter (specific positions)
        'triple-letter': [
            [1, 5], [1, 9],
            [5, 1], [5, 5], [5, 9], [5, 13],
            [9, 1], [9, 5], [9, 9], [9, 13],
            [13, 5], [13, 9]
        ],
        
        // Double Letter (specific positions)
        'double-letter': [
            [0, 3], [0, 11],
            [2, 6], [2, 8],
            [3, 0], [3, 7], [3, 14],
            [6, 2], [6, 6], [6, 8], [6, 12],
            [7, 3], [7, 11],
            [8, 2], [8, 6], [8, 8], [8, 12],
            [11, 0], [11, 7], [11, 14],
            [12, 6], [12, 8],
            [14, 3], [14, 11]
        ]
    };
    
    // Center position for the star
    const centerRow = 7;
    const centerCol = 7;
    
    // Initialize the board
    function initializeBoard() {
        board.innerHTML = '';
        
        for (let row = 0; row < 15; row++) {
            for (let col = 0; col < 15; col++) {
                const cell = document.createElement('div');
                cell.className = 'cell';
                cell.dataset.row = row;
                cell.dataset.col = col;
                
                // Check if this is the center star
                if (row === centerRow && col === centerCol) {
                    cell.classList.add('star');
                    cell.textContent = '★';
                }
                // Check for triple word
                else if (isSpecialCell(row, col, specialCells['triple-word'])) {
                    cell.classList.add('triple-word');
                    cell.textContent = 'TW';
                }
                // Check for double word
                else if (isSpecialCell(row, col, specialCells['double-word'])) {
                    cell.classList.add('double-word');
                    cell.textContent = 'DW';
                }
                // Check for triple letter
                else if (isSpecialCell(row, col, specialCells['triple-letter'])) {
                    cell.classList.add('triple-letter');
                    cell.textContent = 'TL';
                }
                // Check for double letter
                else if (isSpecialCell(row, col, specialCells['double-letter'])) {
                    cell.classList.add('double-letter');
                    cell.textContent = 'DL';
                }
                // Regular cell
                else {
                    cell.classList.add('regular');
                    cell.textContent = '';
                }
                
                board.appendChild(cell);
            }
        }
    }
    
    // Helper function to check if a position is in the special cells array
    function isSpecialCell(row, col, positions) {
        return positions.some(pos => pos[0] === row && pos[1] === col);
    }
    
    // Generate a new puzzle with animation
    async function generatePuzzleAnimated() {
        if (isGenerating) return;
        
        isGenerating = true;
        generateBtn.disabled = true;
        generateBtn.textContent = 'Generating...';
        
        // Clear the board
        clearBoard();
        
        try {
            const eventSource = new EventSource('/api/puzzle/generate-animated');
            
            eventSource.addEventListener('generation_started', function(event) {
                console.log('Puzzle generation started');
            });
            
            eventSource.addEventListener('progress_update', function(event) {
                const progressData = JSON.parse(event.data);
                // Progress updates are ignored since we removed the progress bar
            });
            
            eventSource.addEventListener('tile_placed', function(event) {
                const placementData = JSON.parse(event.data);
                animateTilePlacement(placementData);
            });
            
            eventSource.addEventListener('word_complete', function(event) {
                const wordData = JSON.parse(event.data);
                console.log(`Word completed: ${wordData.word} (${wordData.direction})`);
            });
            
            eventSource.addEventListener('delay', function(event) {
                const delayData = JSON.parse(event.data);
                console.log(`Delay: ${delayData.duration}ms`);
                // The delay is handled by the backend, we just log it
            });
            
            eventSource.addEventListener('generation_complete', function(event) {
                const puzzleData = JSON.parse(event.data);
                currentPuzzle = puzzleData;
                
                displayRemainingTiles();
                updateStats();
                
                eventSource.close();
                isGenerating = false;
                generateBtn.disabled = false;
                generateBtn.textContent = 'Generate New Puzzle';
            });
            
            eventSource.onerror = function(error) {
                console.error('EventSource error:', error);
                eventSource.close();
                isGenerating = false;
                generateBtn.disabled = false;
                generateBtn.textContent = 'Generate New Puzzle';
                alert('Failed to generate puzzle. Please try again.');
            };
            
        } catch (error) {
            console.error('Error generating puzzle:', error);
            alert('Failed to generate puzzle. Please try again.');
            isGenerating = false;
            generateBtn.disabled = false;
            generateBtn.textContent = 'Generate New Puzzle';
        }
    }
    
    // Handle puzzle events
    function handlePuzzleEvent(data) {
        if (data.type === 'tile_placed') {
            animateTilePlacement(data);
        } else if (data.type === 'word_complete') {
            console.log(`Word completed: ${data.word} (${data.direction})`);
        }
    }
    
    // Animate tile placement
    function animateTilePlacement(placementData) {
        const { row, col, letter, points, word, direction, position, totalTiles, letterMultiplier, wordMultiplier } = placementData;
        const cell = board.querySelector(`[data-row="${row}"][data-col="${col}"]`);
        
        if (cell) {
            // Create animated tile element
            const tileElement = document.createElement('div');
            tileElement.className = 'animated-tile';
            
            // Add multiplier classes if applicable
            if (letterMultiplier > 1) {
                tileElement.classList.add(`multiplier-${letterMultiplier}x`);
            }
            if (wordMultiplier > 1) {
                tileElement.classList.add(`word-multiplier-${wordMultiplier}x`);
            }
            
            // Create tile content with multiplier indicators
            let tileContent = `
                <div class="tile-letter">${letter}</div>
                <div class="tile-points">${points}</div>
            `;
            
            // Add letter multiplier indicator if applicable
            if (letterMultiplier > 1) {
                tileContent += `<div class="multiplier-indicator">${letterMultiplier}x</div>`;
            }
            
            // Add word multiplier indicator if applicable
            if (wordMultiplier > 1) {
                tileContent += `<div class="word-multiplier-indicator">${wordMultiplier}x</div>`;
            }
            
            tileElement.innerHTML = tileContent;
            
            // Add word info for debugging
            tileElement.title = `Word: ${word} (${direction}, ${position + 1}/${totalTiles})`;
            
            // Position the animated tile
            tileElement.style.position = 'absolute';
            tileElement.style.top = '0';
            tileElement.style.left = '0';
            tileElement.style.width = '100%';
            tileElement.style.height = '100%';
            tileElement.style.zIndex = '10';
            
            // Add to cell
            cell.appendChild(tileElement);
            
            // Trigger animation with a delay based on position in word
            setTimeout(() => {
                tileElement.classList.add('tile-placed');
                
                // After animation completes, update the cell permanently
                setTimeout(() => {
                    cell.classList.add('has-tile');
                    
                    // Add multiplier classes to cell if applicable
                    if (letterMultiplier > 1) {
                        cell.classList.add(`multiplier-${letterMultiplier}x`);
                    }
                    if (wordMultiplier > 1) {
                        cell.classList.add(`word-multiplier-${wordMultiplier}x`);
                    }
                    
                    // Create final tile content
                    let finalTileContent = `
                        <div class="tile-letter">${letter}</div>
                        <div class="tile-points">${points}</div>
                    `;
                    
                    // Add letter multiplier indicator if applicable
                    if (letterMultiplier > 1) {
                        finalTileContent += `<div class="multiplier-indicator">${letterMultiplier}x</div>`;
                    }
                    
                    // Add word multiplier indicator if applicable
                    if (wordMultiplier > 1) {
                        finalTileContent += `<div class="word-multiplier-indicator">${wordMultiplier}x</div>`;
                    }
                    
                    cell.innerHTML = finalTileContent;
                }, 300); // Match the CSS animation duration
            }, position * 150); // 150ms delay between each letter in a word
        }
    }
    
    // Clear the board
    function clearBoard() {
        const cells = board.querySelectorAll('.cell');
        cells.forEach(cell => {
            cell.classList.remove('has-tile');
            // Keep the special cell text (TW, DW, TL, DL, ★)
            if (!cell.textContent) {
                cell.textContent = '';
            }
        });
    }
    
    // Generate a new puzzle (non-animated)
    async function generatePuzzle() {
        try {
            const response = await fetch('/api/puzzle/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to generate puzzle');
            }
            
            currentPuzzle = await response.json();
            displayPuzzle();
        } catch (error) {
            console.error('Error generating puzzle:', error);
            alert('Failed to generate puzzle. Please try again.');
        }
    }
    
    // Display the puzzle
    function displayPuzzle() {
        if (!currentPuzzle) return;
        
        // Display board
        displayBoard();
        
        // Display remaining tiles
        displayRemainingTiles();
        
        // Update stats
        updateStats();
    }
    
    // Display the board with placed tiles
    function displayBoard() {
        const boardData = currentPuzzle.board;
        const cells = boardData.cells;
        
        for (let row = 0; row < 15; row++) {
            for (let col = 0; col < 15; col++) {
                const cell = board.querySelector(`[data-row="${row}"][data-col="${col}"]`);
                const cellData = cells[row][col];
                
                if (cellData.hasTile) {
                    cell.classList.add('has-tile');
                    
                    // Add multiplier classes if applicable
                    if (cellData.letterMultiplier > 1) {
                        cell.classList.add(`multiplier-${cellData.letterMultiplier}x`);
                    }
                    if (cellData.wordMultiplier > 1) {
                        cell.classList.add(`word-multiplier-${cellData.wordMultiplier}x`);
                    }
                    
                    // Create tile content with multiplier indicators
                    let tileContent = `
                        <div class="tile-letter">${cellData.letter}</div>
                        <div class="tile-points">${cellData.points}</div>
                    `;
                    
                    // Add letter multiplier indicator if applicable
                    if (cellData.letterMultiplier > 1) {
                        tileContent += `<div class="multiplier-indicator">${cellData.letterMultiplier}x</div>`;
                    }
                    
                    // Add word multiplier indicator if applicable
                    if (cellData.wordMultiplier > 1) {
                        tileContent += `<div class="word-multiplier-indicator">${cellData.wordMultiplier}x</div>`;
                    }
                    
                    cell.innerHTML = tileContent;
                } else {
                    cell.classList.remove('has-tile');
                    // Remove any multiplier classes
                    cell.classList.remove('multiplier-2x', 'multiplier-3x', 'word-multiplier-2x', 'word-multiplier-3x');
                    // Keep the special cell text (TW, DW, TL, DL, ★)
                    if (!cell.textContent) {
                        cell.textContent = '';
                    }
                }
            }
        }
    }
    
    // Display remaining tiles
    function displayRemainingTiles() {
        remainingTilesDiv.innerHTML = '';
        
        if (currentPuzzle && currentPuzzle.remainingTiles) {
            currentPuzzle.remainingTiles.forEach(tile => {
                const tileDiv = document.createElement('div');
                tileDiv.className = 'tile';
                
                const letter = tile.letter === ' ' ? '' : tile.letter;
                const points = tile.pointValue;
                
                tileDiv.innerHTML = `
                    <div class="letter">${letter}</div>
                    <div class="points">${points}</div>
                `;
                
                remainingTilesDiv.appendChild(tileDiv);
            });
        }
    }
    
    // Update statistics
    function updateStats() {
        if (currentPuzzle) {
            placedCountSpan.textContent = currentPuzzle.placedTileCount || 0;
            remainingCountSpan.textContent = currentPuzzle.remainingTileCount || 0;
        }
    }
    
    // Event listeners
    generateBtn.addEventListener('click', generatePuzzleAnimated);
    
    showSolutionBtn.addEventListener('click', function() {
        if (currentPuzzle) {
            alert('This is a puzzle! Try to figure out what words you can make with your 7 tiles!');
        } else {
            alert('Generate a puzzle first!');
        }
    });
    
    // Initialize the board on page load
    initializeBoard();
    
    // Generate initial puzzle
    generatePuzzleAnimated();
    
    console.log('Scrabble puzzle board initialized!');
}); 