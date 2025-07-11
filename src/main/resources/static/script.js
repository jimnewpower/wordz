document.addEventListener('DOMContentLoaded', function() {
    const board = document.getElementById('scrabbleBoard');
    const generateBtn = document.getElementById('generatePuzzle');
    const showSolutionBtn = document.getElementById('showSolution');
    const remainingTilesDiv = document.getElementById('remainingTiles');
    const placedCountSpan = document.getElementById('placedCount');
    const remainingCountSpan = document.getElementById('remainingCount');
    
    let currentPuzzle = null;
    
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
    
    // Generate a new puzzle
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
                    cell.innerHTML = `
                        <div class="tile-letter">${cellData.letter}</div>
                        <div class="tile-points">${cellData.points}</div>
                    `;
                } else {
                    cell.classList.remove('has-tile');
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
    
    // Update statistics
    function updateStats() {
        placedCountSpan.textContent = currentPuzzle.placedTileCount;
        remainingCountSpan.textContent = currentPuzzle.remainingTileCount;
    }
    
    // Event listeners
    generateBtn.addEventListener('click', generatePuzzle);
    
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
    generatePuzzle();
    
    console.log('Scrabble puzzle board initialized!');
}); 