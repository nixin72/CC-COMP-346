# Chapter 10: Virtual Memory
- Background
- Demand Paging
- Copy-on-write
- Page replacement
- Allocation of frames
- Thrashing
- Memory-mapped files
- Allocating kernel memory
- OS examples

## Background
- Code needs to be in memory to execute, but entire program rarely used
  - Error code, unusual routines, large data structures
- Entire program code not needed at time
- Consider ability to execute partially-loaded program
  - Program no longer contrained by limits of physical memory
  - Each program takes less memory while running -> more programs run at the same time
    - Increased CPU utilization and throughput with no increase in response time or turnaround
  - Less IO needed to load or swap programs into memory -> each user program runs faster

### Virtual memory
- Separation of logical memory from physical memory
  - Only part of the program needs to be in memory for execution
  - Logical address space can therefore be much larger than physical address space
  - Allows address space to be shared by several processes
  - Allows for more efficient process creation
  - More programs running concurrently
  - Less IO needed to load or swap processes
- Logical view of how process is stored in memory
  - Usually start at address 0, contiguous addresses until end of space
  - Meanwhile, physical memory organized into page frames
  - MMU must map logical to physical
- Virtual memory can be implemented via:
  - Demand paging
  - Demand segmentation

#### Virtual Address Space
- Usually design logical address space for stack to start at Max logical address and grow "down" while head grows "up"
  - Maximizes address space use
  - Unused address space between two is hole
    - No physical memory needed until heap or stack grows to a given new page
  - Enables sparse address spaces with holes left for growth, dynamically linked libraries, etc
  - System libraries shared via mapping into virtual address space
  - Shared memory by mapping pages read-write into virtual address space
  - Pages can be shared during fork() speeding process creation.

## Demand Paging
- Could bring entire process into memory at load time
- Or bring a page into memory only when it is needed
  - Less IO needed, no unnecessary IO
  - Less memory needed
  - Faster response
  - More users
- Similar to paging system with swapping
- Page is needed => reference to it
  - Invalid reference => abort
  - Not in memory => bring to memory
- Lazy swapper - never swaps a page into memory unless page will be needed
  - Swapper that deals with pages is a pager
- Could bring entire process into memory at load time
- Or bring a page into memory only when it is needed:
  - Less IO needed, no unnecessary IO
  - Less memory needed
  - Faster response
  - More users
- Similar to paging systems with swapping

### Basic Concepts
- With swapping, pager guesses which pages will be used before swapping out again
- Instead, pager brings only those pages into memory
- How to determine that set of pages?
  - Need new MMU functionality to implement demand paging
- If pages needed are already memory resident
  - No different from non-demand paging
- If page needed an not memory resident
  - Need to detect and load the page into memory from storage
    - Without changing program behavior
    - Without programmer needing to change code

### Valid-Invalid Bit
- With each page table entry a valid-invalid bit is associated
  (**v** => in memory - memory resident, **i** => not in memory)
- Initially valid-invalid is set to `i` on all entries
- During MMU address translation, if valid-invalid bit in page table entry is **i** => page fault

#### Steps in handling page fault
1. If there is a reference to a page, first reference to that page will trap to OS
  - Page fault
2. OS looks at another table to decide
  - Invalid reference => abort
  - Just not in memory
3. Find free frame
4. Swap page into frame via scheduled disk operation
5. Reset tables to indicate page now in memory
  - Set validation bit = **v**
6. Restart the instruction that caused a page fault

### Aspects of Demand Paging
- Exreme case - start process with *no* pages in memory
  - OS sets instruction pointer to first instruction of process, non-memory-resident -> page fault
  - And for every other process pages on first access
  - **Pure demand paging**
- Actually, a given instruction could access mulitple pages -> multiple page faults
  - Consider fetch and decode instruction which adds 2 number from memory and stores result back in memory
  - Pain decreased because of locality of reference
- Hardware support needed for demand paging
  - Page table with valid/invalid bit
  - Secondary memory (swap device with swap space)
  - Instruction restart

#### Instruction restart
- Consider an instruction that could access several different locations
  - Block move
  - Auto increment/decrement location
  - Restart the whole operation
    - What if source and destination overlap?

#### Free frame list
- When a page fault occurs, the OS must bring desired page from secondary storage into main memory.
- Most OSs maintain a **free-frame list** - a pool of free frames for satisfying such requests
- OS typically allocates free frames using a technique known as **zero-fill-on-demand** - the content of the frames zeroes out before being allocated
- When a system starts up, all available memory is placed on the free-frame list.

### Stages in Demand Paging - Worst Case
1. Trap the OS
2. Save the user registers and process state
3. Determine that the interrupt was a page fault
4. Check that the page reference was legal and determine the location of the page on the disk
5. Issue a read from the disk to a free frame:
  1. Wait in a queue for this device until the read request is serviced
  2. Wait for the device seek and/or latency time
  3. Begin the transfer of the page to a free frame
6. While waiting, allocate the CPU to some other user
7. Receive an interrupt from the disk IO subsystem (IO completed)
8. Save the registers and process state for the other user
9. Determine that the interrupt was read from disk
10. Correct the page table and other tables to show page is now in memory
11. Wait for the CPU to be allocaated to this process again
12. Restore the user registers, process state, and new page table, and then resume the interrupted instruction

### Performance of Demand Paging
- Three major activities:
  - Service the interrupt - careful coding means just several hundred instructions needed
  - Read the page - lots of time
  - Restart the process - again just a small amount of time
- Page fault rate 0 <= p <= 1
  - If p = 0 no page faults
  - If p = 1 every reference is a fault
- Effctive access time (EAT)
  - EAT = (1-p) x memory access
    + p (page fault overhead
    + swap page out
    + swap page in)

### Demand Paging Example
- Memory access time = 200 nanoseconds
- Average page-fault service time = 8 milliseconds
- EAT = (1-p) * 200 + p(8)
- If one access out of 1000 causes a page fault, then EAT = 8.2 microseconds
  - Slowdown factor of 40%!
- If want performance degradation < 10%
  - 220 > 200 + 7,999,800 * p
    20 > 7,999,800 * p
  - p < 0.0000025
  - < one page fault in every 400,000 memory accesses

### Demand Paging Optimizations
- Swap space IO faster than file system IO even if on the same device
  - Swap allocaated in larger chunks, less management needed than file system
- Copy entire process image to swap space at process load time
  - Then page in and out of swap space
  - Used in older BSD Unix
- Demand page in from program binary on disk, but discard rather than paging out when freeing frames
  - Used in Solaris and current BSD
  - Still need to write to swap space
    - Pages not associated with a file (like stack and heap) - **anonymous memory**
    - Pages modified in memory but not yet written back to the file system
- Mobile systems
  - Typicall don't support swapping
  - Instead, demand page from file system and reclaim read-only pages (such as code)

## Copy-on-write
- Allows both parent and child processes to initially **share** the same pages in memory
  - If either process modifies a shared page, only then is the page copied
- Allows more efficient process creation as only modified pages are copied
- In general, free pages are allocated from a pool of **zero-fill-on-demand** pages
  - Pool should always have free frames for fast demand page execution
    - Don't want to have a free frame as well as other processing on page faault
  - Why zero-out a page before allocating it?
- `vfork()` variation on `fork()` system call has parent syspend and child using copy-on-write address space of parent
  - Designed to have child call `exec()`
  - Very efficient

### What happens if there is no free frame?
- Used up by process pages
- Also in demand from the kernel, IO buffers, etc
- How much to allocate to each?
- Page replacement - find some page in memory, but not really in use, page it out
  - Algorithm - terminate? Swap out? Replace the page?
  - Performance - want an algorithm which will result in minimum number of page faults
- Same page may be brought into memory several times

## Page Replacement
- Prevent **over allocation** of memory by modifying page-fault service routine to include page replacement
- Use **modify (dirty) bit** to reduce overhead of page transfers - only modified pages are written to disk
- Page replacement completes separation between logical memory and physical memory - large virtual memory can be provided on a smaller physical memory

### Basic page replacement
1. Fine the location of the desired page on disk
2. Find a free frame
  - If there is a free frame, use it
  - If there is no free frame, use a page replacement algorihtm to select a **victim frame**
    - Write victim frame to disk if dirty
3. Bring the desired page into the new newly free frame; update the page and frame tables
4. Continue the process by restarting the instruction that caused the trap

Note now potentially 2 page transfers for page fault - increasing EAT

### Page and Frame Replacement Algorithms
- **Frame-allocation alogirhtm** determines:
  - How many frames to give each process
  - Which frames to replace
- Page replacement alogirhtm
  - Want lowest page fault rate on both first access and re-access
- Evaluate algorithm by running it on a particular string of memory references (reference string) and computing the numebr of page faults on that string
  - String is just page numbers, not full address
  - Repeated access to the same page does not cause a page fault
  - Results depend on the number of frames available

#### First-In-First-Out (FIFO) algorithm
- Reference string: 7,0,1,2,0,3,0,4,2,3,0,3,0,3,2,1,2,0,1,7,0,1
- 3 frames (3 pages can be in memory at a time per process)
  15 page faults
- Can vary by reference string: consider 1,2,3,4,1,2,5,1,2,3,4,5
  - Adding more frames can cause more page faults!
    - Belady's anomaly
- How to track ages of pages?
  - Just use a FIFO queue

#### Optimal Algorithm
- Replace page that will not be used for longest period of time
  - 9 is optimal for this example
- How do you know this?
  - Can't read the future
- Used for measuring how well your algorithm is doing

#### Least Recently Used (LRU) Algorithm
- Use past knowledge rather than future
- Replace page that has not been used in the most recent amount of time
- Associate time of last use with each page
- 12 faults - better than FIFO but worse than OPT
- Generally good algorithm and frequently used
- But how to implement?
- Counter implementation:
  - Every page entry has a counter; every time page is referenced through this entry, copy the clock into the counter
  - When a page needs to be changed, look at the counters to find smallest value
    - Search through table needed
- Stack implementation
  - Keep a stack of page numbers in a double link form:
  - Page referenced:
    - Move it to the top
    - Requires 6 pointers to be changed
  - But each update more expensive
  - No search for replacement
- LUR and OPT are cases of **stack algorithms** that don't have Belady's Anomaly


#### LRU Approximation algorithms
- LRU needs special hardware and still slow
- **Reference bit**
  - With each page associate a bit, initially = 0
  - When page is reference bit set to 1
  - Replace any reference bit = 0 (if one exists)
    - We do not know the order however
- **Second chance algorith**
  - Generally FIFO, plus hardware provided reference bit
  - Clock replacement
  - If page to be replaces has:
    - Reference bit = 0 -> replace it
    - Reference bit = 1 then:
      - set reference bit to 0, leave page in memory
      - Replace next page, subject to same rules

##### Enhanced Second-Chance Algorithm
- Imrpove algorithm by using reference bit an modify bit (if available) in concert
- Take ordered pair (reference, modify)
  - (0,0) neither recently used not modified - best page to replace
  - (0,1) not recently used but modified - not quite as good, must write out before replacement
  - (1,0) recently used but clean - probably will be used again soon
  - (1,1) recently used and modified - probably will be used again soon and need to write out before replacement
- When page replacement called for, use the clock scheme but use the four classes replace page in lowest non-empty class
  - Might need to search circular queue several times

#### Counting algorithms
- Keep a counter of the number of page references that have been made to each page
  - Not common
- Least Frequently Used (LFU) Algorithm: replaces page with smallest count
- Most Frequently Used (MFU) Algorithm: based on the argument that the page with the smallest count was probably brought in and has yet to be used

#### Page-Buffering Algorithms
- Keep a pool of free frames, always
  - The frame available when needed, not found at fault time
  - Read page into free frame and select victim to evict and add to free pool
  - When convenient, evict victim
- Possibly, keep list of modified pages
  - When backing store otherwise idle, write pages there and set to non-dirty
- Possibly, keep free frame contents intact and note what is in them.
  - If referenced again before reused, no need to load contents again from disk
  - Generally useful to reduce penalty if wrong victim frame selected

### Applications and Page Replacement
- All of these algorithms have OS guessing about future page access
- Some applications have better knowledge - ie. databases
- Memory intensive applications can cause double buffering
  - OS keeps copy of page in memory as IO buffer
  - Application keeps page in memory for its own work
- OS can given direct access to the disk, getting out of the way of the application
  - Raw disk mode
- Bypasses buffering, locking, etc

## Allocation of frames
- Each process needs *minimum* number of frames
- Example: IBM 370 - 6 pages to handle SS MOVE instruction
  - Instruction is 6 bytes, might span 2 pages
  - 2 pages to handle *from*
  - 2 pages to handle *to*
- **Maximum** of course is total frames in the system
- Type major allocation schemes
  - Fixed allocation
  - Priority allocation
- Many variations

### Fixed Allocation
- Equal allocation - for example, if there are 100 frames (after allocating frames for the OS) and 5 processes, give each process 20 frames
  - Keep some as free frame buffer pool
- Proportional allocation - allocate according to the size of process
  - Dyanmic as degree of multiprogramming, process sizes change
    - s_i = size of process p_i
    - S = \sum{s_i}
    - m = total number of frames
    - a_i = alloaction for p_i = s_i\over{S} * m
    - m = 62
    - s_1 = 10
    - s_2 = 127
    - a_1 = 10/137 * 62 = 4
    - a_2 = 127/137 * 62 = 57

### Global vs Local Allocation
- **Global Replacement** - process selects a replacement frame from the set of all frames; one process can take a frame from another
  - But then proces execution time can vary greatly
  - But greater throughput so more common
- **Local Replacement** - each process selects only its own set of allocation frames
  - More consistent per-process performance
  - But possibly underutilized memory

### Reclaiming Pages
- A strategy to implement global page-replacement policy
- All memory requests are satisfied from the free-frame list rahter than waiting for the list to drop to zero before we begin selecting pages for replacement
- Page replacement is triggered when the list falls below a certain threshold
- This strategy attempts to ensure there is always sufficient free memory to satisfy new requests

### Non-Uniform Memory Access
- So far all memory accessed equally
- Many systems are NUMA - speed of access to memory varies
  - Consder system boards containing CPUs and memory, interconnected over a system bus
- Optimal performance comes from allocating memory close to the CPU on which the thread is scheduled
  - And modifying the scheduler to schedule the thread on the same system board when possible
  - Solved by Solaris by creating **lgroups**
    - Structure to track CPU/Memory low latecny groups
    - Used my schedule and pager
    - When possible schedule all threads of a process and allocate all memory for that process within the group

## Thrashing
- If a process does not have "enough" pages, the page fault rate is very high
  - Page fault to get a page
  - Replace existing frame
  - But quickly need replaced frame back
  - This leads to:
    - Low CPU utilization
    - OS thinking it needs to increase the degree of multiprogramming
    - Another process added to the system

### Demand Paging and Thrashing
- Why does demand paging work?
  - Locality model
  - Process migrates from one locality to another
  - Localities may overlap
- Why does thrashing occur?
  - \sum{\text{size of locality > total memory size}}
- Limit effects by using local or priority page replacement

### Working-Set Model
- \delta = working-set window = a fixed number of page references
  Example: 10,000 instructions
- WSS_i (working set of process P_i) = total number of pages referenced in the most recent \delta (varies in time)
  - if \delta too small will not emcompass entire locality
  - if \delta too large will encompass several localities
  - if \delta = \infinity => will encompass entire program
- D = \sum{WSS_i} = total demand frames
  - Approximation of locality
- if D > m => thrashing
- Policy if D > m, then suspend or swap out one of the processes

#### Keeping Track of the Working Set
- Approximate with interval timer + a reference bit
- Example: \delta = 10,000
  - Timer interupts after 5,000 time units
  - Keep in memory 2 bits for each page
  - Whenever a timer interrupts copy and sets the values of all reference bits to 0
  - If one of the bits in memory = 1 -> page in working set
- Why is this not completely accurate?
- Improvement = 10 bits and interrupt every 1000 time units

### Page-Fault Frequency
- More direct approach than WSS
- Establish "acceptable" page fault frequency rate and use local replacement policy
  - If actual rate is too low, process loses frame
  - If actual rate is too high, process gains frame

## Allocating Kernel Memory
- Treated differently from user memory
- Often allocated from a free-memory pool
  - Kernel requests memory for structures of varying sizes
  - Some kernel memory needs to be contiguous
    - ie. for drice IO

### Buddy System
- Allocated memory from fixed size segment consisting of physically contiguous pages
- Memory allocated using power of 2 allocator
  - Satisfies requests in units sized as power of 2
  - Request rounded up to next highest power of 2
  - When smaller allocation needed than is available, current chunk split into two buddies of next-lower power of 2
    - Continue until appropriate sized chunk available
  - Ex. assume 256kb chunk available, kernel requests 21kb
    - Split into A_l and A_r of 128kb each
      - One further divided into B_l and B_r of 64kb each
        - One further into C_l and C_r of 32kb each - one used to satify request
  - Advantage - quickly coalesce unused chunks into larger chunk
  - Disadvantage - fragmentation

### Slab Allocator
- Alternate strategy
- Slab is one or more physically contiguous pages
- Cache consists of one or more slabs
- Single cache for each unique kernel data structures
  - Each cache filled with objects - instantiations of the data structure
- When cache created, filled with objects marked as free
- When structures stored, objects marked as used
- If slab is full of used objects, next object allocated from empty slab
  - If no empty slabs, new slab allocated
- Benefits include no fragmentation, fast memory request satisfaction

#### Slab allocator in Linux
- For example process descriptor is of type `struct tast_struct`
- Approximately 1.7kb of memory
- New taks -< allocate new struct from cache
  - Will use existing free `struct tast_struct`
- Slab can be in three possible states
  1. Full - all used
  2. Empty - all free
  3. Partial - mix of free and used
- Upon request, slab allocator:
  1. Uses free struct in partial slab
  2. If none, takes one from empty slab
  3. If no empty slab, create new empty
- Slab started in Solaris, now wide-spread for both kernel mode and user memor in various OSs
- Linux 2.2 had SLAB, now has both SLOB and SLUB allocators
  - Simple List Of Blocks - maintains 3 list objects for small, medium and large objects
  - SLUB id performance-optimized SLAB removes per-CPU queues, metadata stored in page structure

### Other Considerations
- Prepaging
- Page size
- TLB reach
- Inverted page table
- Program structure
- IO interlock and page locking

### Prepaging
- To reduce the large number or page faults that occur at process startup
- Prepage all or some of the pages a process wil need, bfore they are referenced
- But if prepaged pages are unused, IO and memory was wasted
- Assume *s* pages are prepaged and a of the pages is used
  - Is cost of s* a save page faults > or < than the cost of prepaging?
    s * (1-a) unnecessary pages?
  - a near zero => prepaging loses

### Page Size
- Sometimes OS designers havbe a choice
  - Especially if running on custom-built CPU
- Page size selection must take into consideration:
  - Fragmentation
  - Page table size
  - Resulution
  - IO overhead
  - Number of page faults
  - Locality
  - TLB size and effectiveness
- Always power of 2, usually in range 2^12 to 2^22
- On average, growing over time

### TLB Reach
- The amount of memory accessible from the TLB
- TLB Reach = (TLB size) * (Page Size)
- Ideally, the working set of each process is stored in the TLB
  - Otherwise there is a high degree of page faults
- Increase the page size
  - This may lead to an increase in fragmentation as not all applications require a large page size
- Provide multiple page sizes
  - This allows applications that require larger page sizes the opportunity to use them without an increase in fragmentation

### Program Structure
  - `int[128, 128] data;`
  - Each row is stored in one page
  - Program 1:
    ```cpp
    for (int j = 0 ; j < 128 ; j++) {
      for (int i = 0; i < 128 ; i++) {
        data[i, j] = 0;
      }
    }
    ```
    128 x 128 = 16384 page faults
  - Program 2:
    ```cpp
    for (int i = 0 ; i < 128 ; i++) {
      for (int j = 0; j < 128 ; j++) {
        data[i, j] = 0;
      }
    }
    ```
    128 page fault

### IO Interlock
- Pages must sometimes be locked into memory
- Consider IO - pages that are used for copying a file from a device must be locked from being selected for eviction by a page replacement algorithm
- Pinning of pages to lock into memory

## OS examples

### Windows
- Uses demang paging with clustering. Clustering brings in pages surrounding the faulting page
- Processes are assigned working set minimum and working set maximum
- Working set min is the minimum number of pages the process is guaranteed to have in memory
- A process may be assigned as many pages up to its working maximum
- When the amount of free memory in the system falls below a threshold, automatic working set trimming is performed to restore the amount of free memory
- Working set trimming removes pages from processes that have pages in execess of their working set minimum

### Solaris
- Maintains a list of free pages to assign faulting processes
- `LotsFree` - threshold parameter (amount of free memory) to begin paging
- `Desfree` - threshold parameter to increasing paging
- `Minfree` - threshold parameter to being swapping
- Paging is performed by `pageout` process
- `Pageout` scans pages using modified clock algorithm
- `Scanrate` is the rate at which pages are scanned. This ranges from `slowscan` to `fastscan`
- `Pageout` is called more frequently depending upon the amount of free memory available
- Priority paging gives priority to process code pages


## Performance of Demand Paging
- Stages in Demand Paging (worst case)
  1. Trap the OS
  2. Save the user registers and process state
  3. Determine that the interrupt was a page fault
  4. Check that the page reference was legal and determine the location of the page on the disk
  5. Issue a read from the disk to a free frame:
    1. Wait in a queue for this device until the read request is serviced
    2. Wait for the device seek and/or latency time
    3. Begin the transfer of the page to a free frame
  6. While waiting, allocate the CPU to some other user
  7. Recieve an interrupt from the disk IO subsystem (IO completed)
